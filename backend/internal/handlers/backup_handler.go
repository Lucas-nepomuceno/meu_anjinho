package handlers

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"backend/internal/models"
	"mime/multipart"
	"net/http"
	"os"
	"github.com/google/uuid"
	"path/filepath"
	"strings"
"database/sql"
)

type BackupHandler struct {
	DB         *sql.DB
	StorageDir string
	BaseURL    string
}

func NewBackupHandler(db *sql.DB) *BackupHandler {
	storageDir := os.Getenv("STORAGE_DIR")
	if storageDir == "" {
		storageDir = "storage"
	}

	baseURL := os.Getenv("BASE_URL")
	if baseURL == "" {
		baseURL = "http://localhost:8080"
	}

	return &BackupHandler{
		DB:         db,
		StorageDir: storageDir,
		BaseURL:    baseURL,
	}
}

func (h *BackupHandler) CreateBackup(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeError(w, http.StatusMethodNotAllowed, "Método não permitido")
		return
	}

	var req models.CreateBackupRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "JSON inválido")
		return
	}

	req.Email = strings.TrimSpace(strings.ToLower(req.Email))

	if req.Email == "" {
		writeError(w, http.StatusBadRequest, "Email é obrigatório")
		return
	}

	codigo, err := gerarCodigoBackup()
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Erro ao gerar código")
		return
	}

	backupID := uuid.NewString()
	
	_, err = h.DB.ExecContext(
		r.Context(),
		`INSERT INTO backups (id, email, codigo) VALUES (?, ?, ?)`,
		backupID,
		req.Email,
		codigo,
	)

	if err != nil {
    log.Println("Erro ao criar backup:", err)
    writeError(w, http.StatusInternalServerError, "Erro ao criar backup")
    return
}

	writeJSON(w, http.StatusCreated, models.CreateBackupResponse{
		BackupCode: codigo,
	})
}

func (h *BackupHandler) UploadBackup(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeError(w, http.StatusMethodNotAllowed, "Método não permitido")
		return
	}

	codigo := strings.TrimPrefix(r.URL.Path, "/api/v1/backups/")
	codigo = strings.TrimSuffix(codigo, "/upload")

	if codigo == "" {
		writeError(w, http.StatusBadRequest, "Código é obrigatório")
		return
	}

	err := r.ParseMultipartForm(100 << 20) // 100 MB
	if err != nil {
		writeError(w, http.StatusBadRequest, "Erro ao ler multipart")
		return
	}

	email := strings.TrimSpace(strings.ToLower(r.FormValue("email")))
	registrosJSON := r.FormValue("registrosJson")

	if email == "" || registrosJSON == "" {
		writeError(w, http.StatusBadRequest, "Email e registrosJson são obrigatórios")
		return
	}

	var upload models.BackupUploadRequest

	if err := json.Unmarshal([]byte(registrosJSON), &upload); err != nil {
		writeError(w, http.StatusBadRequest, "registrosJson inválido")
		return
	}

	var backupID string

	err = h.DB.QueryRowContext(
		r.Context(),
		`SELECT id FROM backups WHERE email = ? AND codigo = ?`,
		email,
		codigo,
	).Scan(&backupID)

	if err != nil {
		writeError(w, http.StatusNotFound, "Backup não encontrado")
		return
	}

	files := r.MultipartForm.File["files"]

	err = h.salvarRegistrosComArquivos(
		r.Context(),
		backupID,
		codigo,
		upload.Registros,
		files,
	)

	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"message": "Backup salvo com sucesso",
	})
}

func (h *BackupHandler) RestoreBackup(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeError(w, http.StatusMethodNotAllowed, "Método não permitido")
		return
	}

	var req models.RestoreRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "JSON inválido")
		return
	}

	req.Email = strings.TrimSpace(strings.ToLower(req.Email))

	var backupID string

	err := h.DB.QueryRowContext(
		r.Context(),
		`SELECT id FROM backups WHERE email = ? AND codigo = ?`,
		req.Email,
		req.BackupCode,
	).Scan(&backupID)
	
	if err != nil {
		writeError(w, http.StatusNotFound, "Backup não encontrado")
		return
	}
	
	rows, err := h.DB.QueryContext(
		r.Context(),
		`
		SELECT id, titulo, descricao, data_criacao
		FROM backup_registros
		WHERE backup_id = ?
		ORDER BY data_criacao DESC, criado_em DESC
		`,
		backupID,
	)
	
	if err != nil {
		writeError(w, http.StatusInternalServerError, "Erro ao buscar registros")
		return
	}
	defer rows.Close()


	var registros []models.RestoreRegistro

	for rows.Next() {
		var registroID string
		var item models.RestoreRegistro

		if err := rows.Scan(
			&registroID,
			&item.Titulo,
			&item.Descricao,
			&item.DataCriacao,
		); err != nil {
			writeError(w, http.StatusInternalServerError, "Erro ao ler registro")
			return
		}

		arquivos, err := h.buscarArquivosDoRegistro(r.Context(), registroID)
		if err != nil {
			writeError(w, http.StatusInternalServerError, "Erro ao buscar arquivos")
			return
		}

		item.Arquivos = arquivos
		registros = append(registros, item)
	}

	writeJSON(w, http.StatusOK, models.RestoreResponse{
		Registros: registros,
	})
}

func (h *BackupHandler) salvarRegistrosComArquivos(
	ctx context.Context,
	backupID string,
	codigo string,
	registros []models.RegistroUpload,
	files []*multipart.FileHeader,
) error {
	tx, err := h.DB.BeginTx(ctx, nil)
	if err != nil {
		return err
	}
	defer tx.Rollback()

	fileIndex := 0

	for _, registro := range registros {
		registroID := uuid.NewString()

		_, err := tx.ExecContext(
			ctx,
			`
			INSERT INTO backup_registros 
			(id, backup_id, titulo, descricao, data_criacao)
			VALUES (?, ?, ?, ?, ?)
			`,
			registroID,
			backupID,
			registro.Titulo,
			registro.Descricao,
			registro.DataCriacao,
		)

		if err != nil {
			return err
		}

		for range registro.Arquivos {
			if fileIndex >= len(files) {
				return fmt.Errorf("quantidade de arquivos enviada menor que a informada no JSON")
			}

			fileHeader := files[fileIndex]
			fileIndex++

			caminhoArquivo, tamanho, mimeType, err := h.salvarArquivoFisico(
				codigo,
				registroID,
				fileHeader,
			)
			if err != nil {
				return err
			}

			arquivoID := uuid.NewString()

			_, err = tx.ExecContext(
				ctx,
				`
				INSERT INTO backup_arquivos
				(id, registro_id, nome_original, caminho_arquivo, tamanho_bytes, mime_type)
				VALUES (?, ?, ?, ?, ?, ?)
				`,
				arquivoID,
				registroID,
				fileHeader.Filename,
				caminhoArquivo,
				tamanho,
				mimeType,
			)

			if err != nil {
				return err
			}
		}
	}

	return tx.Commit()
}

func (h *BackupHandler) salvarArquivoFisico(
	codigo string,
	registroID string,
	fileHeader *multipart.FileHeader,
) (string, int64, string, error) {
	src, err := fileHeader.Open()
	if err != nil {
		return "", 0, "", err
	}
	defer src.Close()

	ext := strings.ToLower(filepath.Ext(fileHeader.Filename))

	if ext != ".jpg" && ext != ".jpeg" && ext != ".png" && ext != ".webp" {
		return "", 0, "", fmt.Errorf("tipo de arquivo não permitido: %s", ext)
	}

	dir := filepath.Join(h.StorageDir, codigo, registroID)

	if err := os.MkdirAll(dir, 0755); err != nil {
		return "", 0, "", err
	}

	nomeSeguro := limparNomeArquivo(fileHeader.Filename)
	destino := filepath.Join(dir, nomeSeguro)

	dst, err := os.Create(destino)
	if err != nil {
		return "", 0, "", err
	}
	defer dst.Close()

	tamanho, err := io.Copy(dst, src)
	if err != nil {
		return "", 0, "", err
	}

	mimeType := detectarMimePorExtensao(ext)

	return destino, tamanho, mimeType, nil
}

func (h *BackupHandler) buscarArquivosDoRegistro(
	ctx context.Context,
	registroID string,
) ([]models.RestoreArquivo, error) {
	rows, err := h.DB.QueryContext(
		ctx,
		`
		SELECT nome_original, caminho_arquivo
		FROM backup_arquivos
		WHERE registro_id = ?
		ORDER BY criado_em ASC
		`,
		registroID,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var arquivos []models.RestoreArquivo

	for rows.Next() {
		var nome string
		var caminho string

		if err := rows.Scan(&nome, &caminho); err != nil {
			return nil, err
		}

		arquivos = append(arquivos, models.RestoreArquivo{
			Nome: nome,
			URL:  fmt.Sprintf("%s/%s", h.BaseURL, strings.ReplaceAll(caminho, "\\", "/")),
		})
	}

	return arquivos, nil
}

func gerarCodigoBackup() (string, error) {
	const chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

	bytes := make([]byte, 12)

	if _, err := rand.Read(bytes); err != nil {
		return "", err
	}

	var builder strings.Builder

	for i, b := range bytes {
		if i > 0 && i%4 == 0 {
			builder.WriteString("-")
		}

		builder.WriteByte(chars[int(b)%len(chars)])
	}

	return builder.String(), nil
}

func limparNomeArquivo(nome string) string {
	nome = filepath.Base(nome)
	nome = strings.ReplaceAll(nome, " ", "_")

	var builder strings.Builder

	for _, r := range nome {
		if (r >= 'a' && r <= 'z') ||
			(r >= 'A' && r <= 'Z') ||
			(r >= '0' && r <= '9') ||
			r == '.' ||
			r == '_' ||
			r == '-' {
			builder.WriteRune(r)
		}
	}

	if builder.Len() == 0 {
		return "arquivo.jpg"
	}

	return builder.String()
}

func detectarMimePorExtensao(ext string) string {
	switch ext {
	case ".png":
		return "image/png"
	case ".webp":
		return "image/webp"
	default:
		return "image/jpeg"
	}
}

func writeJSON(w http.ResponseWriter, status int, data any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(data)
}

func writeError(w http.ResponseWriter, status int, message string) {
	writeJSON(w, status, map[string]string{
		"error": message,
	})
}
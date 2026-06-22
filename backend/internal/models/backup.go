package models

type RegistroUpload struct {
	LocalID     int      `json:"localId"`
	Titulo      string   `json:"titulo"`
	Descricao   string   `json:"descricao"`
	DataCriacao string   `json:"dataCriacao"`
	Arquivos    []string `json:"arquivos"`
}

type BackupUploadRequest struct {
	Registros []RegistroUpload `json:"registros"`
}

type CreateBackupRequest struct {
	Email string `json:"email"`
}

type CreateBackupResponse struct {
	BackupCode string `json:"backupCode"`
}

type RestoreRequest struct {
	Email      string `json:"email"`
	BackupCode string `json:"backupCode"`
}

type RestoreArquivo struct {
	Nome string `json:"nome"`
	URL  string `json:"url"`
}

type RestoreRegistro struct {
	Titulo      string           `json:"titulo"`
	Descricao   string           `json:"descricao"`
	DataCriacao string           `json:"dataCriacao"`
	Arquivos    []RestoreArquivo `json:"arquivos"`
}

type RestoreResponse struct {
	Registros []RestoreRegistro `json:"registros"`
}
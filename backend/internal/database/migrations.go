package database

import "database/sql"

func RunMigrations(db *sql.DB) error {
	queries := []string{
		`PRAGMA foreign_keys = ON;`,

		`CREATE TABLE IF NOT EXISTS backups (
			id TEXT PRIMARY KEY,
			email TEXT NOT NULL,
			codigo TEXT NOT NULL UNIQUE,
			criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
		);`,

		`CREATE TABLE IF NOT EXISTS backup_registros (
			id TEXT PRIMARY KEY,
			backup_id TEXT NOT NULL,
			titulo TEXT NOT NULL,
			descricao TEXT NOT NULL,
			data_criacao TEXT NOT NULL,
			criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (backup_id) REFERENCES backups(id) ON DELETE CASCADE
		);`,

		`CREATE TABLE IF NOT EXISTS backup_arquivos (
			id TEXT PRIMARY KEY,
			registro_id TEXT NOT NULL,
			nome_original TEXT NOT NULL,
			caminho_arquivo TEXT NOT NULL,
			tamanho_bytes INTEGER NOT NULL,
			mime_type TEXT NOT NULL,
			criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
			FOREIGN KEY (registro_id) REFERENCES backup_registros(id) ON DELETE CASCADE
		);`,
	}

	for _, query := range queries {
		if _, err := db.Exec(query); err != nil {
			return err
		}
	}

	return nil
}
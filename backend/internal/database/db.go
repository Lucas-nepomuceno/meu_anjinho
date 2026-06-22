package database

import (
	"database/sql"
	"os"

	_ "modernc.org/sqlite"
)

func Connect() (*sql.DB, error) {
	dbPath := os.Getenv("SQLITE_PATH")

	if dbPath == "" {
		dbPath = "meuanjinho.db"
	}

	db, err := sql.Open("sqlite", dbPath)
	if err != nil {
		return nil, err
	}

	if err := db.Ping(); err != nil {
		return nil, err
	}

	return db, nil
}
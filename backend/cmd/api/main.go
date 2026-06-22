package main

import (
	"log"
	"backend/internal/database"
	"backend/internal/handlers"
	"net/http"
	"os"
	"strings"
)

func main() {
	db, err := database.Connect()
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	if err := database.RunMigrations(db); err != nil {
		log.Fatal(err)
	}

	backupHandler := handlers.NewBackupHandler(db)

	mux := http.NewServeMux()

	mux.HandleFunc("/api/v1/backups", backupHandler.CreateBackup)
	mux.HandleFunc("/api/v1/backups/restore", backupHandler.RestoreBackup)

	mux.HandleFunc("/api/v1/backups/", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost && strings.HasSuffix(r.URL.Path, "/upload") {
			backupHandler.UploadBackup(w, r)
			return
		}

		http.NotFound(w, r)
	})

	mux.Handle(
		"/storage/",
		http.StripPrefix(
			"/storage/",
			http.FileServer(http.Dir("storage")),
		),
	)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	log.Println("Servidor rodando na porta", port)

	if err := http.ListenAndServe(":"+port, mux); err != nil {
		log.Fatal(err)
	}
}
package storage

import (
	"fmt"

	"github.com/genjidb/genji"
	"go.uber.org/zap"
)

// Genji storage backend
type Genji struct {
	db *genji.DB
}

// OpenGenji opens a new GenjiDB storage.
func OpenGenji(path string) (*Genji, error) {
	db, err := genji.Open(path)
	if err != nil {
		return nil, fmt.Errorf("opening GenjiDB: %w", err)
	}

	if err := (migrator{db}).execute(); err != nil {
		return nil, fmt.Errorf("applying migrations: %w", err)
	}

	return &Genji{db}, nil
}

// Close closes the database and logs any errors which occur during said process.
func (g *Genji) Close() {
	if err := g.db.Close(); err != nil {
		zap.S().Errorw("closing GenjiDB failed", "component", "storage", "error", err)
	}
}

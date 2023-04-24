package main

import (
	"fmt"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/storage"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

func main() {
	initLogger()
	defer zap.S().Sync()

	genji := mustGenji()
	defer genji.Close()
}

func initLogger() {
	cfg := zap.NewProductionConfig()
	cfg.DisableCaller = true
	cfg.DisableStacktrace = true
	cfg.EncoderConfig.EncodeDuration = zapcore.StringDurationEncoder
	cfg.EncoderConfig.EncodeTime = zapcore.ISO8601TimeEncoder

	l, err := cfg.Build()
	if err != nil {
		panic(fmt.Sprintf("building logger: %s", err))
	}

	zap.ReplaceGlobals(l)
}

func mustGenji() *storage.Genji {
	genji, err := storage.OpenGenji("./db")
	if err != nil {
		zap.S().Fatalf("opening storage: %s", err)
	}

	return genji
}

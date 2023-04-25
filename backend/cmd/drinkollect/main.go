package main

import (
	"crypto/ecdsa"
	"crypto/x509"
	"encoding/pem"
	"flag"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/app"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"google.golang.org/grpc"
	"google.golang.org/grpc/keepalive"
	"google.golang.org/grpc/reflection"
)

var (
	jwtKeyPath string
	gRPCAddr   string
)

const (
	maxConnectionIdle       = time.Minute
	gracefulShutdownTimeout = time.Second * 5
)

func main() {
	initLogger()
	defer zap.S().Sync()

	parseFlags()

	// Initialize authorizer
	jwtKey := mustJWTKey(jwtKeyPath)
	authorizer := mustAuthorizer(jwtKey)

	// Initialize database
	genji := mustGenji()
	defer genji.Close()

	// Initialize gRPC service
	service := app.NewService(genji, authorizer)

	// Create and start gRPC server. If an error arrives on the server channel, we'll immediately stop the program.
	listener, server := mustGRPC(gRPCAddr, service, authorizer)
	gRPCCh := startGRPCServer(listener, server)

	zap.S().Infow("all components initialized, gRPC server listening", "addr", gRPCAddr)

	// Listen for shutdown signals
	exitCh := make(chan os.Signal, 1)
	signal.Notify(exitCh, os.Interrupt, syscall.SIGTERM)

	select {
	case err := <-gRPCCh:
		zap.S().Errorw("critical error while serving gRPC, will perform shutdown", "error", err)
	case <-exitCh:
		zap.S().Infow("gracefully shutting down all components")
	}

	// Launch graceful shutdowns and wait for some time
	shutdownDone := make(chan struct{})
	go func() {
		defer close(shutdownDone)
		server.GracefulStop()
	}()

	select {
	case <-shutdownDone:
	case <-time.After(gracefulShutdownTimeout):
	}

	// Now, actually force the shutdown
	server.Stop()
	<-gRPCCh
	<-shutdownDone
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

func parseFlags() {
	flag.StringVar(&jwtKeyPath, "jwt-key", "", "Path to ECDSA private key used for signing JWTs")
	flag.StringVar(&gRPCAddr, "grpc", ":18081", "Bind address for the gRPC server")
	flag.Parse()
}

func mustJWTKey(path string) *ecdsa.PrivateKey {
	data, err := os.ReadFile(path)
	if err != nil {
		zap.S().Fatalf("reading JWT private key from %s: %s", path, err)
	}

	block, _ := pem.Decode(data)
	if block == nil {
		zap.S().Fatalf("failed to decode JWT private key from %s as PEM", path)
	}

	key, err := x509.ParseECPrivateKey(block.Bytes)
	if err != nil {
		zap.S().Fatalf("parsing JWT private key from %s as ECDSA private key: %s", path, err)
	}

	return key
}

func mustAuthorizer(key *ecdsa.PrivateKey) *auth.Authorizer {
	authorizer, err := auth.NewAuthorizer(key)
	if err != nil {
		zap.S().Fatalf("creating authorizer with JWT key: %s", err)
	}

	return authorizer
}

func mustGenji() *db.Genji {
	genji, err := db.OpenGenji("./db")
	if err != nil {
		zap.S().Fatalf("opening storage: %s", err)
	}

	return genji
}

func mustGRPC(addr string, service *app.Service, authorizer *auth.Authorizer) (net.Listener, *grpc.Server) {
	listener, err := net.Listen("tcp", addr)
	if err != nil {
		zap.S().Fatalf("listening on gRPC bind address %s: %s", addr, err)
	}

	// Basic gRPC server with limited idle
	server := grpc.NewServer(
		grpc.KeepaliveParams(keepalive.ServerParameters{MaxConnectionIdle: maxConnectionIdle}),
		grpc.UnaryInterceptor(authorizer.UnaryInterceptor(
			"/drinkollect.v1.Drinkollect/Login",
			"/drinkollect.v1.Drinkollect/Register",
		)),
	)
	reflection.Register(server)
	service.RegisterPB(server)

	return listener, server
}

func startGRPCServer(listener net.Listener, server *grpc.Server) chan error {
	ch := make(chan error)
	go func() {
		ch <- server.Serve(listener)
		close(ch)
	}()

	return ch
}

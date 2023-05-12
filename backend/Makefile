.generate-proto:
	protoc \
		--go_opt=module=github.com/irdkwmnsb/DrinkKollect/backend --go_out=. \
		--go-grpc_opt=module=github.com/irdkwmnsb/DrinkKollect/backend --go-grpc_out=. \
		api/v1/drinkollect.proto

generate: .generate-proto

build:
	go build -o bin/drinkollect cmd/drinkollect/main.go

run: build
	./bin/drinkollect -jwt-key ./jwt-key.pem -db ./db

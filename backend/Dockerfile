FROM golang:1.20 as builder

WORKDIR /build

COPY ./go.mod .
COPY ./go.sum .
RUN go mod download

COPY ./cmd cmd
COPY ./internal internal

RUN CGO_ENABLED=0 go build -o app ./cmd/drinkollect

FROM scratch
COPY --from=builder /build/app /usr/local/drinkollect/app

WORKDIR /usr/local/drinkollect
ENTRYPOINT [ "/usr/local/drinkollect/app" ]

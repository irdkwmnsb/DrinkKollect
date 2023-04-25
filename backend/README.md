# Drinkollect backend
Go service running a single gRPC server for the API calls.

- [GenjiDB](https://github.com/genjidb/genji) as the database for the simplicity of deployment
- Authorization based on JWTs signed using ecdsa 

## Deployment
1. Generate a private key to be used
   ```bash
   openssl ecparam -name prime256v1 -genkey -noout -out jwt-key.pem
   ```
2. Run the service using docker-compose
   ```bash
   docker-compose up --build -d
   ```

# ---------- Stage 1: build Angular ----------
FROM node:18-alpine AS build
WORKDIR /app

# Copia i file di package e installa le dipendenze
COPY package*.json ./
RUN npm ci

# Copia sorgenti e builda per sottocartella /aimusei/
COPY . .
# NOTA: aggiungo anche --deploy-url per prefissare gli URL degli asset
RUN NODE_OPTIONS=--openssl-legacy-provider npm run build -- --configuration production --base-href /aimusei/

# ---------- Stage 2: serve con Nginx ----------
FROM nginx:alpine

# Config Nginx per SPA in sottocartella
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia gli artefatti dentro /usr/share/nginx/html/aimusei/
# Sostituisci "aimusei" se il nome della cartella in dist è diverso
COPY --from=build /app/dist/aimusei/ /usr/share/nginx/html/aimusei/

EXPOSE 80

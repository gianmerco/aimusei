# Dockerfile per Angular App
FROM node:16-alpine as build

WORKDIR /app

# Copia i file di package e installa le dipendenze
COPY package*.json ./
RUN npm install

# Copia il codice sorgente e builda l'app
COPY . .
RUN npm run build --prod --base-href /aimusei

# Fase di produzione
FROM nginx:alpine

# Copia i file buildati in nginx
COPY --from=build /app/dist/aimusei /usr/share/nginx/html

# Copia la configurazione nginx personalizzata
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
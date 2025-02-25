FROM clojure:latest AS builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y nodejs npm
RUN npm install

ARG API_URL
ENV API_URL=${API_URL}
RUN npx shadow-cljs release app


FROM nginx:alpine
COPY --from=builder /app/resources/public /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

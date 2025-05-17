# Build stage
FROM clojure AS builder
WORKDIR /usr/src/app

# Copy only inputs for dependency resolution
COPY deps.edn build.clj /usr/src/app/
RUN clojure -T:build uber || true

# Then copy the full app and re-run
COPY . /usr/src/app/
RUN clojure -T:build uber

# Runtime stage
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /usr/src/app/target/duckt-server-standalone.jar app.jar
CMD ["java", "-jar", "app.jar"]

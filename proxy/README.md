# Duckt Proxy

## Run

```bash
lein run
```

## Run with Docker

Build it:
```bash
docker build -t duckt-proxy .
```

Run:
```bash
docker run --rm -it -p 4445:4445 \
-e DUCKT_SERVER_URL=http://host.docker.internal:4444 \
-e PROXY_TOKEN=v1:dcbf1... \
duckt-proxy
```

### Local development run with Docker

```bash
# This adds your localhost as host.docker.internal inside the container.
# Make sure that you set the value http://host.docker.internal:4444 in the 
# Duckt control panel server target URL field for this proxy
docker run --add-host=host.docker.internal:host-gateway \
--rm -it -p 4445:4445 \
-e DUCKT_SERVER_URL=http://host.docker.internal:4444 \
-e PROXY_TOKEN=v1:dcbf1... \ # get your token from Duckt
duckt-proxy
```

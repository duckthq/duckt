VERSION := $(shell git describe --tags --abbrev=0 2>/dev/null)

docker-build-arm64:
	docker build --platform linux/arm64 -t duckt-proxy-arm64:local .

docker-tag-arm64: docker-build-arm64
	docker tag duckt-proxy-arm64:local duckthq/proxy-arm64:$(VERSION)
	docker tag duckt-proxy-arm64:local duckthq/proxy-arm64:latest

docker-push-arm64: docker-tag-arm64
	docker push duckthq/proxy-arm64:$(VERSION)
	docker push duckthq/proxy-arm64:latest

docker-build:
	docker build --platform linux/amd64 -t duckt-proxy:local .

docker-tag: docker-build
	docker tag duckt-proxy:local duckthq/proxy:$(VERSION)
	docker tag duckt-proxy:local duckthq/proxy:latest

docker-push: docker-tag
	docker push duckthq/proxy:$(VERSION)
	docker push duckthq/proxy:latest

docker-publish: docker-build docker-tag docker-push

docker-publish-arm64: docker-build-arm64 docker-tag-arm64 docker-push-arm64

docker-publish-all: docker-publish docker-publish-arm64

.PHONY: build tag push

configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

docker-build-arm64:
	docker build --platform linux/arm64 -t duckt-server-arm64:local .

docker-tag-arm64: configure docker-build-arm64
	docker tag duckt-server-arm64:local duckthq/server-arm64:$(VERSION)
	docker tag duckt-server-arm64:local duckthq/server-arm64:latest

docker-push-arm64: configure docker-tag-arm64
	docker push duckthq/server-arm64:$(VERSION)
	docker push duckthq/server-arm64:latest

docker-build:
	docker build --platform linux/amd64 -t duckt-server:local .

docker-tag: configure docker-build
	docker tag duckt-server:local duckthq/server:$(VERSION)
	docker tag duckt-server:local duckthq/server:latest

docker-push: configure docker-tag
	docker push duckthq/server:$(VERSION)
	docker push duckthq/server:latest

docker-publish: docker-build docker-tag docker-push

docker-publish-arm64: docker-build-arm64 docker-tag-arm64 docker-push-arm64

docker-publish-all: docker-publish docker-publish-arm64

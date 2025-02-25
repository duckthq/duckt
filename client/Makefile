.PHONY: build tag push

configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

configure-api:
	$(eval export API_URL=$(shell bash -c 'read -p "API URL: " API URL; echo $$api_url'))

docker-build-arm64:
	docker build --platform linux/arm64 --build-arg API_URL=https://api.duckt.dev -t duckt-web-arm64 .

docker-tag-arm64: configure docker-build-arm64
	docker tag duckt-web-arm64:local duckthq/web-arm64:$(VERSION)
	docker tag duckt-web-arm64:local duckthq/web-arm64:latest

docker-push-arm64: configure docker-tag-arm64
	docker push duckthq/web-arm64:$(VERSION)
	docker push duckthq/web-arm64:latest

docker-build:
	docker build --platform linux/amd64 --build-arg API_URL=https://api.duckt.dev -t duckt-web:local .

docker-tag: configure docker-build
	docker tag duckt-web:local duckthq/web:$(VERSION)
	docker tag duckt-web:local duckthq/web:latest

docker-push: configure docker-tag
	docker push duckthq/web:$(VERSION)
	docker push duckthq/web:latest

docker-publish: docker-build docker-tag docker-push

docker-publish-arm64: docker-build-arm64 docker-tag-arm64 docker-push-arm64

docker-publish-all: docker-publish docker-publish-arm64

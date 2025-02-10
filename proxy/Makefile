.PHONY: build tag push

configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

docker-build:
	docker build --platform linux/amd64 -t duckt-proxy:local .

docker-tag: build configure
	docker tag duckt-proxy:local duckthq/proxy:$(VERSION)
	docker tag duckt-proxy:local duckthq/proxy:latest

docker-push: configure tag
	docker push duckthq/proxy:$(VERSION)
	docker push duckthq/proxy:latest

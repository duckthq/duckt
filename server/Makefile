.PHONY: build tag push

configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

docker-build:
	docker build -t duckt-server:local .

docker-tag: build configure
	docker tag duckt-server:local duckthq/server:$(VERSION)
	docker tag duckt-server:local duckthq/server:latest

docker-push: tag
	docker push duckthq/server:$(VERSION)
	docker push duckthq/server:latest

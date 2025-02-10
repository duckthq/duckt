.PHONY: build tag push

configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

docker-build:
	docker build --platform linux/amd64 -t duckt-server:local .

docker-tag: build configure
	docker tag duckt-server:local duckthq/server:$(VERSION)
	docker tag duckt-server:local duckthq/server:latest

docker-push: configure tag
	docker push duckthq/server:$(VERSION)
	docker push duckthq/server:latest

all: docker-push

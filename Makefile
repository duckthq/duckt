configure:
	$(eval export VERSION=$(shell bash -c 'read -p "Version: " version; echo $$version'))

release: configure
	git tag -a $(VERSION)
	gh release create

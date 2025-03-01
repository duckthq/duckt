.PHONY: release

# Utility functions
define echo_step
	@echo "▶ $(1)"
endef

define echo_success
	@echo "✓ $(1)"
endef

define print_tag
	@echo "Current tag: $(shell git describe --tags --abbrev=0)"
endef

define print_logo
	@echo "                                                                                                    "
	@echo "############################                                                                        "
	@echo "################################                                                                    "
	@echo " #################################                                                 ###              "
	@echo "  ##################################         ##########                           ####        ####  "
	@echo "   ##################################        ###   ######  ###    ###     #####   ####    ## #######"
	@echo "        #############################        ###      #### ###    ###   ######### ####  #### #######"
	@echo "         #############################       ###      #### ###    ###  ####   ### #########   ####  "
	@echo "          ###########################        ###      #### ###    ### ####        ########    ####  "
	@echo "           ##########################        ###    #####  ###   ####  ####   ### #########   ####  "
	@echo "                 ###################         ###########   ###########  ######### ####  ##### ######"
	@echo "                 #################           #######         ###   ###    #####    ###    ###   ### "
	@echo "                  ##############                                                                    "
	@echo "                                                                                                    "
	@echo ""
endef

define print_build_info
	@echo "───────────────────────────────────────────"
	@echo "Build Info:"
	@echo "• Build time: $(shell date)"
	@echo "• Git branch: $(shell git rev-parse --abbrev-ref HEAD)"
	@echo "• Git commit: $(shell git rev-parse --short HEAD)"
	@echo "───────────────────────────────────────────"
endef

# Version prompt
define get_version
	@echo "Enter version for new release (e.g., 1.2.3):"
	@read VERSION && echo "Creating release for version: $$VERSION" && echo $$VERSION > .version
endef

release:
	$(call print_logo)
	$(call print_tag)
	$(call print_build_info)
	@echo "Enter version for new release (e.g., 1.2.3):"
	@read VERSION && \
		echo "Creating release for version: $$VERSION" && \
		echo "▶ Starting release process for version: $$VERSION" && \
		echo "▶ Creating git tag $$VERSION..." && \
		git tag -a $$VERSION -m "Release $$VERSION" && \
		echo "✓ Tag created successfully!" && \
		echo "▶ Pushing to the repository..." && \
		git push --tags origin main && \
		echo "✓ Pushed to the repository successfully!" && \
		echo "" && \
		echo "  ╔═══════════════════════════════════════════╗" && \
		echo "  ║                                           ║" && \
		echo "  ║  🦆 DUCKT.DEV RELEASED SUCCESSFULLY! 🦆   ║" && \
		echo "  ║                                           ║" && \
		echo "  ╚═══════════════════════════════════════════╝" && \
		echo "Quack!" && \
		echo ""

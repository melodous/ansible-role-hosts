APP					= ansible-hosts
ROOT				= $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
REQ					= requirements.txt
VIRTUALENV			?= $(shell which virtualenv)
PYTHON				?= $(shell which python2.7)
PIP					?= $(shell which pip2.7)
MOLECULE_PROVIDER	= openstack
VENV				?= $(ROOT)/.venv
PLATFORMS			= rhel6 rhel7

.ONESHELL:
.PHONY: test test_rhel6 test_rhel7 clean venv $(VENV)

all: default

default:
	@echo
	@echo "Welcome to '$(APP)' software package:"
	@echo
	@echo "usage: make <command>"
	@echo
	@echo "commands:"
	@echo "    clean                           - Remove generated files and directories"
	@echo "    venv                            - Create and update virtual environments"
	@echo "    test PLATFORM=($(PLATFORMS))    - Run test on specified platform"
	@echo "    del PLATFORM=($(PLATFORMS))     - Remove specified platform"
	@echo "    ansiblelint 					   - Run ansible-lint validations
	@echo "    yamllint 					   - Run yamlint validations
	@echo

venv: $(VENV)

$(VENV): $(REQ)
	@echo ">>> Initializing virtualenv..."
	mkdir -p $@; \
	[ -z "$$VIRTUAL_ENV" ] && $(VIRTUALENV)  --no-site-packages  --distribute -p $(PYTHON) $@; \
	$@/bin/pip install --exists-action w -r $(REQ);
	@echo && echo && echo && echo

linkrole:
	@mkdir -p roles/ ; rm -rf roles/$(APP) 2>/dev/null; ln -sf ../ roles/$(APP)

ansiblelint: venv linkrole
	@echo ">>> Executing ansible lint..."
	@[ -z "$$VIRTUAL_ENV" ] && source $(VENV)/bin/activate; \
	ansible-lint -r ansible-lint -r $(VENV)/lib/python2.7/site-packages/ansiblelint/rules playbook.yml
	@echo

yamllint: venv linkrole
	@echo ">>> Executing yaml lint..."
	[ -z "$$VIRTUAL_ENV" ] && source $(VENV)/bin/activate; \
	yamllint tasks/* vars/* defaults/* meta/* handlers/*
	@echo

lint: yamllint ansiblelint

delete:
	@echo ">>> Deleting $(PLAFORM) ..."
	[ -z "$$VIRTUAL_ENV" ] && source $(VENV)/bin/activate; \
	molecule destroy --platform=$(PLATFORM) --provider=$(MOLECULE_PROVIDER)
	@echo

test: venv linkrole
	@echo ">>> Runing $(PLAFORM) tests ..."
	[ -z "$$VIRTUAL_ENV" ] && source $(VENV)/bin/activate; \
	PYTEST_ADDOPTS="--junit-xml junit-$(PLATFORM).xml --ignore roles/$(APP)" molecule test --platform=$(PLATFORM) --provider=$(MOLECULE_PROVIDER);
	@echo

clean:
	@echo ">>> Cleaning temporal files..."
	rm -rf .cache/
	rm -rf $(VENV)
	rm -rf junit-*.xml
	rm -rf tests/__pycache__/
	rm -rf .vagrant/
	rm -rf .molecule/
	rm -rf roles/
	@echo

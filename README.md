hosts
================
[![Build Status](http://10.95.82.44:8080/buildStatus/icon?job=ansible-hosts/ansible-hosts-master/ansible-hosts-master-build)](http://10.95.82.44:8080/job/ansible-hosts/job/ansible-hosts-master/job/ansible-hosts-master-build/)

This adds hosts entries.

## Requirements

None.

## Role Variables
	
	hosts_default_interface: none

## Dependencies

None.

## Example Playbook

    - hosts: db-servers
      roles:
        - { role: ansible-hosts }


## License

(c)2016 Telefónica S.A. All rights reserved.

## Author Information

This role was created in 2015 by [Raul Melo](https://pdihub.hi.inet/rmf390).

Tests added by [Oscar Erades](https://pdihub.hi.inet/b-oedq).

#Testing

This role is designed to be tested with molecule (https://github.com/metacloud/molecule), performing the tests with testinfra(https://github.com/philpep/testinfra).

For more details, [check how automatic tests are defined](https://wikis.hi.inet/InnovationDO/index.php/Ansible_roles_and_automatic_testing).
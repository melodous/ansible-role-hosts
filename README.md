Welcome to hosts Ansible Role’s documentation!
==============================================

hosts
-----

Ansible role to manage /etc/hosts entries, it can automagically create
entries for all server on the ansible inventory or create custom
entries.

### Requirements

N/A

### Dependencies

N/A

### Example Playbook

    - hosts: servers
      roles:
        - { role: hosts }

yum hosts ansible role default variables
----------------------------------------

#### Sections

-   hosts entries management

### hosts entries management

`hosts_add_inventory`

> If true create intreis on /etc/hosts per each hosts in ansible
> inventory

    hosts_add_inventory: false

`additional_hosts_entries`

> Dictionary with custom entries for /etc/hosts file

    additional_hosts_entries: false

Example:

    additional_hosts_entries:
      - name: yum_server
        ip: 192.168.1.200

Changelog
---------

**hosts**

This project adheres to Semantic Versioning and human-readable
changelog.

### hosts master - unreleased

##### Added

-   First addition

##### Changed

-   First change

### hosts v0.0.1 - 2017/07/12

##### Added

-   Initial version

Copyright
---------

hosts

Copyright (C) DATE Raul Melo &lt;<raul.melo@opensolutions.cloud>&gt;

LICENSE

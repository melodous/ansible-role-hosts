hosts
=====

Ansible role to manage /etc/hosts entries, it can automagically create entries for all server on the ansible inventory or create custom entries.

Requirements
------------

N/A

Dependencies
------------

N/A

Example Playbook
----------------

.. code::

  - hosts: servers
    roles:
      - { role: hosts }

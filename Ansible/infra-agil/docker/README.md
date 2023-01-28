Docker
=========

Instalação do docker ce em Debian/CentOS

Role Variables
--------------

**packages_remove:** Pacotes para ser removidos em ambas distros


Example Playbook
----------------

Exemplo para execução da role


```yml
- hosts: servers
  become: yes
  roles:
  - docker
```


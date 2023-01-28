Lighttpd
=========

Instalação do Lighttpd em Debian/CentOS

Role Variables
--------------

- **user:** Usuário do lighttpd
- **group:** Grupo do lighttpd


Example Playbook
----------------

Exemplo de playbook a ser executada 

```yml
 - hosts: docker
   roles:
      - lighttpd
```
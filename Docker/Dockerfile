FROM alpine

EXPOSE 8080

ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_USER=user \
    DB_PASS=pass \
    DB_NAME=db

#CASO EXISTA A VARIAVEL O SCRIPT DO entrypoint IRÁ ALTERAR O COMPORTAMENTO DA APLICAÇÃO ANTES DE INICIAR
#MEMCACHED_HOST = m1:11211,m2:11211 # opcional

COPY . /opt/app

RUN apk add php-cli php-mysqli php-session php81-pecl-memcached mysql-client \
    && adduser -h /opt/app -s /bin/sh -D -g 'Container user' php  \
    && chown -R php: /opt/app

#COMENTANDO O USUÁRIO PARA TRABALHAR COM ENTRYPOINT
#USER php

WORKDIR /opt/app

ENTRYPOINT [ "/opt/app/docker-entrypoint.sh" ]
#CMD php -S 0.0.0.0:8080
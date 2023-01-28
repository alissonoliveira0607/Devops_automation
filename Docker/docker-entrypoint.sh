#!/bin/sh

if [ -n "$MEMCACHED_HOST" ]; then
    #altera php.ini
    sed -Ei 's/(session.save_handler).*/\1 = memcached/' /etc/php81/php.ini
    sed -Ei "s/;(session.save_path).*/\1 = \"$MEMCACHED_HOST\"/" /etc/php81/php.ini
fi

exec php -S 0.0.0.0:8080 && mysql -h mariadb -u devops -p4linux devops < /opt/app/db/dump.sql 


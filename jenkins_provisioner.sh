#!/bin/bash

rm -rf .git*

for X in 100 200; do
    sed -i s/Distribution/Jenkins/ index.html
    scp -i $KEYFILE -r $WORKSPACE $USERNAME@172.27.11.$X:/tmp/html
    LIGHTTPD=$(ssh -i $KEYFILE $USERNAME@172.27.11.$X 'echo $(which apt &> /dev/null && echo www-data || echo lighttpd)')
    ssh -i $KEYFILE $USERNAME@172.27.11.$X "sudo rm -rf /srv/www/html && sudo mv /tmp/html /srv/www && sudo chown -R $LIGHTTPD: /srv/www/html"
done
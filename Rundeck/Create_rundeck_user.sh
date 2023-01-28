#!/bin/sh

#Criando o user rundeck com shell script fazendo o discover das maquinas com vagrant status e implementando a lógica



KEY="$(vagrant ssh automation -c 'sudo cat /root/keys/rundeck.pub' < /dev/null)"
read -r -d '' CMD <<EOF
sudo useradd -r -s /bin/bash -m -d /var/lib/rundeck rundeck;
echo "rundeck ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/rundeck;
sudo mkdir -p /var/lib/rundeck/.ssh;
echo '$KEY' | sudo tee /var/lib/rundeck/.ssh/authorized_keys;
sudo chown -R rundeck: /var/lib/rundeck/.ssh
EOF
for M in $(vagrant status | grep 'running' | cut -d' ' -f1); do
vagrant ssh $M -c "$CMD";
done
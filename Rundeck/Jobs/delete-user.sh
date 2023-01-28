#!/bin/bash



getent passwd @option.user@ > /dev/null
if [ $? != 0 ]; then
     echo 'Usuário não existe'
else
    deluser --remove-all-files @option.user@ || userdel -r @option.user@
    echo 'Usuário excluido com sucesso!'
fi
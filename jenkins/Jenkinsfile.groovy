pipeline {
    agent any

    stages {
        
        
            
        //     steps{
                
        //         //Limpa o WORKSPACE
        //         cleanWs()
                
            
        //     }
        // }
        
        // stage('Fetch') {
        //     steps {
        //         //CLONA O REPO PRIVADO DO gitea   
        //        //git credentialsId: 'gitea', url: 'git@172.27.11.10:devops/site.git'
               
        //        //Clona o repo público do git
        //        git 'https://github.com/alissonoliveira0607/site.git'
               
        //        //COMANDO CHAMA O SHELL SCRIPT E REMOVE OS ARQUIVOS OCULTOS DO git
        //        sh 'rm -rf .git*'
        //     }
        // }
        
        stage('Deploy'){
            steps{
                
                //remove diretório .git
                sh 'rm -rf .git*'

                //Substitui o que for Distribution por Jenkins no arquivo index.html
                sh "sed -i 's/Distribution/Jenkins - Pipeline/' index.html"
                
                //Define as variaveis para acessos contendo as credenciais de acesso utilizada pelo jenkins
                withCredentials([sshUserPrivateKey(credentialsId: 'jenkins', keyFileVariable: 'keyfile', usernameVariable: 'username')]) {
                    
                    //Definindo o bloco script para trabalhar com variaveis
                    script{
                        
                        
                        //Definindo uma lista com o range de ip que sera percorrido e chamando a função each
                        [100,200].each {
                        //Copia os arquivos do workspace para o hospedeiro
                        sh "scp -i ${keyfile} -r $WORKSPACE ${username}@172.27.11.${it}:/tmp/html"
                        
                        //Atribui o retorno do comando para a variavel lighttpd como se fosse um subshell
                        lighttpd = sh(returnStdout: true, script: "ssh -i ${keyfile} ${username}@172.27.11.${it} 'echo `which apt &> /dev/null && echo www-data || echo lighttpd`'").trim()   //o trim() remove os espaços e quebras de linha indesejados
                        
                        //Conecta na maquina, remove os arquivos depois move novamente setando as permissões necesários de acordo com o user seja www-data ou lighttpd
                        sh "ssh -i ${keyfile} ${username}@172.27.11.${it} 'sudo rm -rf /srv/www/html && sudo mv /tmp/html /srv/www && sudo chown -R ${lighttpd}: /srv/www/html'"
                        
                        //sh "scp -i ${keyfile} -r $WORKSPACE ${username}@172.27.11.200:/tmp/html"
                        //lighttpd = sh(returnStdout: true, script: "ssh -i ${keyfile} ${username}@172.27.11.200 'echo `which apt &> /dev/null && echo www-data || echo lighttpd`'")
                        //sh "ssh -i ${keyfile} ${username}@172.27.11.200 'sudo rm -rf /srv/www/html && sudo mv /tmp/html /srv/www && sudo chown -R ${lighttpd}: /srv/www/html'"                    
                        
                        }
                    }
                }
            }
        }
        stage('Clean'){
             steps{
                
                //Limpa o WORKSPACE
                 cleanWs()
                
            
            }
        }        
    }
}

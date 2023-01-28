node {

    //definindo um array de servers para balancear os testes
    def servers = [100,200]

    def server = servers[new Random().nextInt(servers.size())]

    //definindo a variavel imagem
    def imagem = 'aoliveirasilva/php-login'
    
    //Criando um array que conterá os parametros referente ao deploy final da aplicação
    def parameters = [
        '--restart always',
        "--name ${JOB_BASE_NAME}",
        "--network ${JOB_BASE_NAME}",
        '--ip 192.168.10.10',
        '--add-host=memcached:172.27.11.30',
        '-e DB_HOST=172.27.11.30',
        '-e DB_PROT=3306',
        '-e DB_NAME=infraagil',
        '-e DB_USER=devops',
        '-e DB_PASS=4linux'


    ]

    try{

        println("Utilizando servidor 172.27.11.${server}")
        docker.withServer("172.27.11.${server}:2375"){
        
            stage('Build') {
                //realizando o clone do repo
                //git branch: 'dev', credentialsId: 'gitea', url: 'git@172.27.11.10:devops/php-login.git'

                //removendo arquivos do GIT
                sh 'rm -rf .git*'


                //criando a imagem
                docker.build(imagem, '-f docker/Dockerfile .')

            }
            stage('Test') {

                //subindo o compose, passando p prefixo e destachando o serviço
                sh "docker-compose -p ${JOB_BASE_NAME} -f docker/docker-compose.yml up -d"

                //definindo um tempo para subida do DB
                sleep 15


                //populando o banco a partir do dump
                sh "docker exec -i ${JOB_BASE_NAME}_mysql_1 mysql -u root -p'Abc123!' php < db/dump.sql"

                //obtendo o ip do conteinere que roda o banco
                ip = sh(returnStdout: true, script: "docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' ${JOB_BASE_NAME}_app_1").trim()

                //subindo uma imagem em alpine para realizar alguns testes com o curl na aplicação
                docker.image('alpine').withRun("--tty --interactive --network ${JOB_BASE_NAME}_default"){ alpine ->
                    sh "docker exec ${alpine.id} apk add --no-cache curl"   //instala o no-cache e o curl
                    sh "docker exec ${alpine.id} curl -sL ${ip} > /dev/null"    //fazendo uma chamada curl na aplicação e jogando a saida em /dev/null
                    output = sh(returnStdout: true, script: "docker exec ${alpine.id} curl -sL --cookie-jar cookie -d 'username=victor@frankenstein.co.uk&pass=123' ${ip}/login.php").trim()    //capturando o token da sessão para realizar o login através do curl
                    if (!output.contains('Bem Vindo!'))     //Validando o login ao aplicação se baseando em uma palavra existente
                        error('Login falhou')

                }

            }
            stage('Save') {

    			docker.withRegistry('https://index.docker.io/v1/', 'dockerregistry') {
				//fazendo o push da imagem para o registry
				docker.image(imagem).push()
    			}                

            }

        }
        stage('Deploy') {
            
            //fazendo um for para executar o deploy em ambos os nodes
            [100, 200].each{
                docker.withServer("172.27.11.${it}:2375"){
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerregistry'){
                        docker.image(imagem).pull()   //baixando a imagem do registry
                    }

                    //criando a rede para o docker
                    sh "docker network create --subnet 192.168.10.0/24 ${JOB_BASE_NAME} || /bin/true"


                    //removendo o conteinere existente
                    sh "docker rm -f ${JOB_BASE_NAME} || /bin/true"

                    //subindo o conteinere - como parameters é uma lista o join realiza o espaçamento com relação a passagem de cada parametro
                    docker.image(imagem).run(parameters.join(' '))

                }
            }
        }

    } catch(ex){
        
        //retornando o erro ao try para forçar a finalização do compose
        throw ex

    } finally{

        docker.withServer("172.27.11.${server}:2375"){
            
            //derrubando o compose e excluindo os volumes
            sh "docker-compose -p ${JOB_BASE_NAME} -f docker/docker-compose.yml down -v"

        }

    }
}

# Projeto Antenas
## Devops - Laboratório Projeto BD

### Integrantes: Leonardo Lins, Marcos Kisto, Bruna.

Link Trello: https://trello.com/b/yYt0V2L3/antenas \
Link burndown chart: https://docs.google.com/spreadsheets/d/13EVLT6vyxxCn-7awbnz2QzUW1QQzmkQq-0VSWZxgUMI/edit?usp=sharing \
Endereço jenkins cloud: http://34.66.76.1:8080/ \
Monitoramento: https://console.cloud.google.com/monitoring/dashboards?authuser=1&project=canvas-verve-240811


Endereço aplicação no ar: http://34.66.76.1:8081

Detalhes de implementações contidos no WIKI deste repositório.


# Projeto Antenas - Empresário

# 1. Source Control Mgmt
Para o versionamento e controle do código fonte, está sendo utilizadas as tecnologias GIT e GITHUB:

# 2. Cloud
A aplicação está sendo hospedada em um servidor atravez do Google Cloud.
#### Instalação e configuração da máquina virtual no Google Cloud
##### 1. Criar uma instância de VM do Debian 9 no GoogleCloud;
Ao colocar a instância em execução, o acesso à VM é feito por SSH pela própria página do googleCloud onde é aberto um terminal. É através desse terminal que serão feitos os passos a seguir para configuração do ambiente e deploy do projeto.
##### 2. Instalar os pacotes no Debian 9:
* Instalar GIT - **sudo apt-get install git**
* Instalar o Docker;
* Instalar o docker-compose;

##### 3. Habilitar o Acesso por HTTP:
* Abrir o GoogleCloud Console;
* Em recursos, clicar em **Compute Engine**;
* Clicar no nome da Instância da VM criada;
* Na parte superior, clicar em **Editar**;
* Rolar a página para baixo e em **Firewalls**, marcar a opção **Permitir Tráfego HTTP**;
* Na política de controle do Firewall, liberar as portas 8080 a 8089, 5000, 9000 e 9990;

# 3. Containers
A inicialização e controle dos containers é realizado através do docker-compose. Para isso foi criado um arquivo docker-compose.yml na pasta "/etc/antenasdocker/". O conteúdo do docker-compose.yml é o seguinte:

` version: "2"` <br>
` services:`<br>
`   webserver:`<br>
`     build:`<br>
`       context: .`<br>
`       dockerfile: Dockerfile_tomcat`<br>
`     container_name: webServer_antenas`<br>
`     ports:`<br>
`       - "8080:8081"`<br>
`       - "8085:8080"`<br>
`     volumes:`<br>
`       - /var/dockerVolumes/jenkins/workspace/antenas-ci/build/libs:/usr/local/tomcat/webapps`<br>
`     depends_on:`<br>
`       - jenkins`<br>
`       - mongo`<br>
`     command: java -jar ./usr/local/tomcat/webapps/antenas-ci.jar`<br>
`     restart: always`<br>
`     `<br><br>
`   jenkins:`<br>
`     image: jenkins/jenkins:lts`<br>
`     container_name: jenkins_antenas`<br>
`     ports:`<br>
`       - "5000:5000"`<br>
`       - "8082:8080"`<br>
`     volumes:`<br>
`       - /var/dockerVolumes/jenkins/:/var/jenkins_home`<br>
`     restart: always`<br>
`     `<br><br>
`   mongo:`<br>
`     image: mongo`<br>
`     container_name: mongo_antenas`<br>
`     environment:`<br>
`       MONGO_INITDB_ROOT_USERNAME: root`<br>
`       MONGO_INITDB_ROOT_PASSWORD: 12345678`<br>
`     restart: always`<br><br>
`   mongo-express:`<br>
`     image: mongo-express`<br>
`     container_name: mongoExrpess_antenas`<br>
`     depends_on:`<br>
`       - mongo`<br>
`     restart: always`<br>
`     ports:`<br>
`       - "8081:8081"`<br>
`     environment:`<br>
`       ME_CONFIG_MONGODB_ADMINUSERNAME: root`<br>
`       ME_CONFIG_MONGODB_ADMINPASSWORD: 12345678`<br>
`       `<br><br>
`   portainer:`<br>
`     image: portainer/portainer`<br>
`     container_name: portainer_antenas`<br>
`     restart: always`<br>
`     ports:`<br>
`       - "9000:9000"`<br>
`       - "8083:8000"`<br>
`     volumes: `<br>
`       - /var/run/docker.sock:/var/run/docker.sock`<br>
`       - /var/dockerVolumes/portainer:/data portainer/portainer`<br><br>
`    grafana:`<br>
`    image: grafana/grafana`<br>
`    container_name: grafana_antenas`<br>
`    restart: always`<br>
`    ports:`<br>
`      - "3000:3000"`<br>
`    volumes:`<br>
`      - /var/dockerVolumes/grafana`<br>
Com esse docker-compose foram criados os seguintes containers:
### 1. Container webServer_antenas
 Necessário para rodar a aplicação e torná-la disponível na web. Este servidor trata-se de um tomcat versão 8.0 e será substituído por um wildfly em uma futura srpint. A aplicação é disponibilizada na porta 8080;
### 2. Container Mongodb 
Utilizado como banco de dados da aplicação internamente através da porta padrão 27017;
### 3. Container MongodbExpress 
Para administração interativa do Mongodb pela porta 8081;
### 4. Conainer Jenkins 
Responsável pelo Continuous Integration. Realiza o deploy automático da aplicação no servidor fazendo o build para que o container "webserver-antenas" execute a aplicação. A administração do jenkins é feita através da porta 8082;
## 5. Container Grafana (Porta 3000)
Esse container se comunica com o Prometheus( Porta 9090), um serviço de monitoramento instalado no host que funciona em conjunto com o netdata( Porta 19990).

##### 4.1. Comunicação do Github para o Jenkins
A comunicação entre o Github e o Jenkins ocorre em função de uma API. 
Depois do item anterior, é necessário configurar o Github para enviar uma requisição para o Jenkins em nosso servidor. Isso é feito seguindo os seguintes passos:
1. Acessar o projeto no Github;
1. Clicar em **Settings**;
1. Clicar em **Webhooks**;
1. Clicar em **Add**;
1. Em **Payload Url**, inserir: http://_Ip_do_Servidor:_porta_Jenkins_>/github-webhook/
1. Em **Content Type** escolher **application/json**;
1. Logo abaixo marcar a opção **Send me everything**; em uma pasta que também é um volume do container antenas-server.

##### 4.2. Configurar o Jenkins com a API do github
1. Abrir o Jenkins;
1. Clicar no nome do Projeto;
1. Clicar em Configurar;
1. Na Aba **Trigger de builds**, marcar a opção "**GitHub hook trigger for GITScm polling**";
1. Na aba **Gerenciamento de código fonte**, em credentials adicionar a credencial do github;

### Inicialização dos containers
A inicialização automática dos continers é configurada executando o comando:

`cronjob -e`

Será aberto um arquivo de texto e no final deve-se acrescentar:

`@reboot docker-comopse up -d`

## 4. Continuous Integration
Para o continuous Integration foi escolhido o Jenkins que após um commit no github, é feito um deploy de um arquivo .jar em um volume compartilhado com o container webserver que é responsável por rodar a aplicação.

## 5. Collaboration
Para a colaboração entre os integrantes do time está sendo utilizado o Trello, e o próprio github para compartilhamento de informações e distribuição das tarefas;

# 6. Monitoring
O monitoramento do projeto será realizado integrando três sistemas:
* Netdata - Realizará o monitoramento em tempo real de várias métricas do host e seus serviços;
* Prometheus - A cada intervalo de tempo (10s) irá consultar o Netdata sobre as métricas e fará histórico dessas 
métricas;
* Grafana - Ferramenta obterá do Prometheus as métricas monitoradas e gerará os Dashboards das métricas monitoradas;

## Instalação do **netdata**
* Instalar dependências: # apt-get install autoconf autoconf-archive autogen automake cmake libjson-c-dev libjudy-dev liblz4-dev libmnl-dev libssl-dev libuv1-dev netcat pkg-config python3-pymongo uuid-dev zlib1g-dev 
* Instalar o Netdata: curl -fsSL https://my-netdata.io/kickstart.sh | bash
### Porta de acesso:
* Liberar no firewall do googleCloud a porta **199990**
### Configurar o Netdata
* executar: nano /etc/netdata/netdata.conf
* No bloco [registry] descomentar a primeira linha e colocar a opção yes (enabled = yes)
* No mesmo bloco descomentar a linha **registry hostname** e colocar o ip (registry to announce = https://34.66.76.1)
Estão sendo utilizadas as métricas do próprio Google Cloud em conjuto de um container **SonarQube**.

## Instalar o Prometheus:
* Instalação conforme tutorial: https://github.com/badtuxx/giropops-monitoring
* Liberar a porta **9090** no googleCloud

## Instalação do Grafana:
*

1. Criar usuário prometheus:
useradd --nocreate-home --shell /bin/false prometheus

## 7. Testing

## 8. Analytics
## 9. Database Automation
## 10. Release Orchestration
## 11. Security
## 12. Configuration
## 13. AIOps



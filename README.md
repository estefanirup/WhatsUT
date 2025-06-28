# WhatsUT

# Compile backend
javac -cp ".;lib\spark-core-2.9.4.jar;lib\gson-2.8.9.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar;lib\javax.servlet-api-4.0.1.jar;lib\jetty-server-9.4.44.v20210927.jar;lib\jetty-http-9.4.44.v20210927.jar;lib\jetty-util-9.4.44.v20210927.jar;lib\jetty-io-9.4.44.v20210927.jar" server\rmi\*.java server\ServerApp.java client\HttpBridge.java utils\*.java

# Inicie servidor RMI
java -cp ".;lib\*" server.ServerApp

# Em outro terminal, inicie HTTP Bridge
java -cp ".;lib\spark-core-2.9.4.jar;lib\gson-2.8.9.jar;lib\slf4j-api-1.7.36.jar;lib\slf4j-simple-1.7.36.jar;lib\javax.servlet-api-4.0.1.jar;lib\jetty-server-9.4.44.v20210927.jar;lib\jetty-http-9.4.44.v20210927.jar;lib\jetty-util-9.4.44.v20210927.jar;lib\jetty-io-9.4.44.v20210927.jar" client.HttpBridge

# Em outro terminal (serve arquivos HTML)
cd client
python -m http.server 5500

# Modo alternativo de iniciar
Rodar o arquivo .\start-backend.bat no diretório

# Visualizar o front
http://localhost:5500/

Login - http://localhost:5500/html/login.html
Registro - http://localhost:5500/html/register.html

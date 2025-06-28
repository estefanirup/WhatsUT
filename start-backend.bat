@echo off
REM Compile Java files
echo Compiling server and client...
javac -cp ".;lib\*" server\rmi\*.java server\ServerApp.java client\HttpBridge.java utils\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed.
    pause
    exit /b
)

REM Start RMI server
echo Starting RMI Server...
start cmd /k "java -cp .;lib\* server.ServerApp"

REM Start HTTP Bridge
echo Starting HTTP Bridge...
start cmd /k "java -cp .;lib\* client.HttpBridge"

REM Start HTTP file server
echo Starting HTML file server on port 5500...
cd client
start cmd /k "python -m http.server 5500"
cd ..
@echo off
REM Configura o caminho para o arquivo .jar
set JAR_PATH="dirdiffexplorer-1.0-SNAPSHOT.jar"

REM Executa o arquivo .jar usando Java
java -jar %JAR_PATH%

REM Pausa o terminal para que você possa ver a saída
pause

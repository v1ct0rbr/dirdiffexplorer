#!/bin/bash
# Configura o caminho para o arquivo .jar
JAR_PATH="dirdiffexplorer-1.0-SNAPSHOT.jar"

# Executa o arquivo .jar usando Java
java -jar "$JAR_PATH"

# Mantém o terminal aberto (apenas necessário se quiser ver a saída e estiver executando em um terminal gráfico que fecha automaticamente)
read -p "Pressione [Enter] para continuar..."

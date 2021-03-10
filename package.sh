#!/bin/bash

source ./script/config.sh

echo "------------------------------ mvn install [start] ------------------------------"
mvn clean install -Dmaven.test.skip=true
echo "------------------------------ mvn install [end] ------------------------------"

echo "------------------------------ package distribute [start] ------------------------------"
cp -f target/IBWebServer.jar ${PROJECT_NAME}
cp -f script/*.sh ${PROJECT_NAME} && chmod +x ${PROJECT_NAME}/*.sh

ls -al ${PROJECT_NAME}
echo "------------------------------ package distribute [end] ------------------------------"
echo "distribute success."

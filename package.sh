#!/bin/bash
git pull
mvn install -Dmaven.test.skip=true
dirname=$1
if [ x"$1" = x ]; then
  dirname='prod'
fi
echo ${dirname}
mkdir -p /opt/webserver/IBWebServer/
cp target/IBWebServer.jar /opt/webserver/IBWebServer/
cp script/restart.sh /opt/webserver/IBWebServer/ && chmod +x /opt/webserver/IBWebServer/restart.sh
cp script/spring-boot.sh /opt/webserver/IBWebServer/ && chmod +x /opt/webserver/IBWebServer/spring-boot.sh
cp script/${dirname}/start.sh /opt/webserver/IBWebServer/ && chmod +x /opt/webserver/IBWebServer/start.sh

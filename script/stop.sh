#!/bin/bash

source ./config.sh $1

for item in ${CLIENT_CONFIG[@]};
do
  client_host=`echo ${item} | awk -F ':' '{print $1}'`

  # 关闭服务
  num=$(ps -ef | grep "${PROJECT_NAME_JAR}" | grep "${ACTIVE}" | grep "${CLIENT_ID}" | grep "${client_host}" | grep -v "grep" | wc -l)
  if [ $num -gt 0 ]; then
    ps -ef | grep "${PROJECT_NAME_JAR}" | grep "${ACTIVE}" | grep "${CLIENT_ID}" | grep "${client_host}" | grep -v "grep" | awk '{print $2}' | xargs kill -9
    echo "[${CUR_TIME}]-${PROJECT_NAME_JAR}: to stop." >> "${PROJECT_CONSOLE_LOG_PATH}/${PROJECT_NAME}.${client_host}.log"
  else
    echo "[${CUR_TIME}]-${PROJECT_NAME_JAR}: not running." >> "${PROJECT_CONSOLE_LOG_PATH}/${PROJECT_NAME}.${client_host}.log"
  fi
done
#!/bin/bash

source ./config.sh $1

for item in ${CLIENT_CONFIG[@]};
do
  client_host=`echo ${item} | awk -F ':' '{print $1}'`
  server_port=`echo ${item} | awk -F ':' '{print $2}'`

  # 启动服务
  num=$(ps -ef | grep "${PROJECT_NAME_JAR}" | grep "${ACTIVE}" | grep "${CLIENT_ID}" | grep "${client_host}" | grep -v "grep" | wc -l)
  if [ $num -gt 0 ]; then
    echo "[${CUR_TIME}]-${PROJECT_NAME_JAR}: is running." >> "${PROJECT_CONSOLE_LOG_PATH}/${client_host}.${PROJECT_NAME}.log"
  else
    echo "[${CUR_TIME}]-${PROJECT_NAME_JAR}: start to running." >> "${PROJECT_CONSOLE_LOG_PATH}/${client_host}.${PROJECT_NAME}.log"

    time_hour=$(date "+%Y%m%d%H%M");
    log_path="${PROJECT_CONSOLE_LOG_PATH}/${client_host}.console.${time_hour}.log"
    nohup java -jar ${PROJECT_NAME_JAR} --spring.profiles.active=${ACTIVE} --my.ib.server.clientId=${CLIENT_ID} --my.ib.server.host=${client_host} --server.port=${server_port} >> ${log_path} 2>&1 &
    sleep 3
    tail -n 50 ${log_path}
  fi
done


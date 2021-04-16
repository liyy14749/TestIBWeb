#!/bin/bash

# 载入java等环境变量
if [ -f "/root/.bash_profile" ]; then
  source "/root/.bash_profile"
fi

# 服务名称
PROJECT_NAME="IBWebServer"
PROJECT_NAME_JAR="${PROJECT_NAME}.jar"
CUR_TIME=$(date "+%Y-%m-%d %H:%M:%S")

# 项目目录
PROJECT_PATH="/opt/webserver/${PROJECT_NAME}"
if [ ! -d "${PROJECT_PATH}" ]; then
  mkdir -p ${PROJECT_PATH}
fi

# 日志目录
PROJECT_LOG_PATH="/opt/weblogs/${PROJECT_NAME}"
PROJECT_CONSOLE_LOG_PATH="${PROJECT_LOG_PATH}/console"
if [ ! -d "${PROJECT_CONSOLE_LOG_PATH}" ]; then
  mkdir -p ${PROJECT_CONSOLE_LOG_PATH}
fi

# 启动环境，默认dev
active="dev"
group_num=2 # 当前股票分组数量
client_id="9999"
client_config=("127.0.0.1:8083")
if [ "$1" == "-prod" ]; then
  active="prod"
  group_num=4 # 当前股票分组数量
  client_id="90001"
  client_config=("10.0.2.27:8727" "10.0.2.28:8828" "10.0.2.29:8929")
elif [ "$1" == "-test" ]; then
  active="test"
  group_num=2 # 当前股票分组数量
  client_id="90001"
  client_config=("127.0.0.1:8083")
fi

echo "active: ${active}"
echo "client_id: ${client_id}"
echo "client_config: ${#client_config[@]}"

# 启动分组
m=0
CONFIG=()
client_config_len=${#client_config[@]}
for i in "${!client_config[@]}"; do
  if [ "${client_config[$i]}" == "10.0.2.27:8727" ]; then
    CONFIG[$m]="${active}:${client_config[$i]}:${client_id}"
    echo "CONFIG[${m}]: ${CONFIG[$m]}"
    m=$((m + 1))
  else
    tmp_client_id=client_id
    for ((j = 0; j < group_num; j++)); do
      tmp_client_id=$((client_id + j))
      if [ "${j}" -eq 0 ]; then
        CONFIG[$m]="${active}:${client_config[$i]}:${tmp_client_id}"
      else
        client_host=$(echo ${client_config[$i]} | awk -F ':' '{print $1}')
        server_port=$(echo ${client_config[$i]} | awk -F ':' '{print $2}')
        server_port=$((server_port + j))
        CONFIG[$m]="${active}:${client_host}:${server_port}:${tmp_client_id}"
      fi
      echo "CONFIG[${m}]: ${CONFIG[$m]}"
      m=$((m + 1))
    done
  fi
done

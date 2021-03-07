#!/bin/bash
nohup java -jar IBWebServer.jar --spring.profiles.active=test >> console.log 2>&1 &
sleep 1
tail -fn 50 console.log
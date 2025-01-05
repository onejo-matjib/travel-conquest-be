#!/bin/bash

DEPLOY_PATH=/home/ubuntu/travel-conquest-be
echo ">>> 배포 경로: $DEPLOY_PATH" >> /home/ubuntu/deploy.log

mkdir -p $DEPLOY_PATH
echo ">>> 디렉토리 생성 완료" >> /home/ubuntu/deploy.log

BUILD_JAR=$(ls /home/ubuntu/travel-conquest-be/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/deploy.log

echo ">>> build 파일 복사" >> /home/ubuntu/deploy.log
cp $BUILD_JAR $DEPLOY_PATH 2>> /home/ubuntu/deploy_err.log

echo ">>> 현재 실행중인 애플리케이션 pid 확인 후 일괄 종료" >> /home/ubuntu/deploy.log
sudo pkill -f 'java -jar'

DEPLOY_JAR=$DEPLOY_PATH/$JAR_NAME
echo ">>> DEPLOY_JAR 배포"    >> /home/ubuntu/deploy.log
echo ">>> $DEPLOY_JAR의 $JAR_NAME를 실행합니다" >> /home/ubuntu/deploy.log
nohup java -jar -Dspring.profiles.active=prod $DEPLOY_JAR >> /home/ubuntu/deploy.log 2>> /home/ubuntu/deploy_err.log &

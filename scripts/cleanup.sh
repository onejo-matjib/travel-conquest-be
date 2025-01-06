#!/bin/bash

DEPLOY_PATH=/home/ubuntu/travel-conquest-be

echo ">>> 기존 JAR 파일 삭제" >> /home/ubuntu/deploy.log
rm -f $DEPLOY_PATH/build/libs/*.jar 2>> /home/ubuntu/deploy_err.log

echo ">>> 기존 배포 디렉토리 정리" >> /home/ubuntu/deploy.log
rm -rf $DEPLOY_PATH/* 2>> /home/ubuntu/deploy_err.log

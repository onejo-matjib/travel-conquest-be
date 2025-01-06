#!/bin/bash
set -e  # 오류 발생 시 스크립트 즉시 종료
set -x  # 실행되는 명령어를 출력

DEPLOY_PATH=/home/ubuntu/travel-conquest-be

echo ">>> 기존 JAR 파일 삭제 시작" >> /home/ubuntu/deploy.log
if [ -d "$DEPLOY_PATH/build/libs" ]; then
    rm -f "$DEPLOY_PATH/build/libs/"*.jar 2>> /home/ubuntu/deploy_err.log
    echo ">>> 기존 JAR 파일 삭제 완료" >> /home/ubuntu/deploy.log
else
    echo ">>> build/libs 디렉토리가 존재하지 않습니다." >> /home/ubuntu/deploy.log
fi

echo ">>> 기존 배포 디렉토리 정리 시작" >> /home/ubuntu/deploy.log
if [ -d "$DEPLOY_PATH" ]; then
    rm -rf "$DEPLOY_PATH"/* 2>> /home/ubuntu/deploy_err.log
    echo ">>> 기존 배포 디렉토리 정리 완료" >> /home/ubuntu/deploy.log
else
    echo ">>> DEPLOY_PATH 디렉토리가 존재하지 않습니다: $DEPLOY_PATH" >> /home/ubuntu/deploy.log
fi

echo ">>> cleanup.sh 스크립트 완료" >> /home/ubuntu/deploy.log

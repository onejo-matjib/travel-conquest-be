#!/bin/bash
set -e  # 오류 발생 시 스크립트 즉시 종료
set -x  # 실행되는 명령어를 출력

DEPLOY_PATH=/home/ubuntu/travel-conquest-be

# 로그 파일 경로 설정
LOG_FILE=/home/ubuntu/deploy.log
ERR_LOG_FILE=/home/ubuntu/deploy_err.log

echo ">>> 기존 JAR 파일 삭제 시작" >> "$LOG_FILE"
if [ -d "$DEPLOY_PATH/build/libs" ]; then
    rm -f "$DEPLOY_PATH/build/libs/"*.jar 2>> "$ERR_LOG_FILE"
    echo ">>> 기존 JAR 파일 삭제 완료" >> "$LOG_FILE"
else
    echo ">>> build/libs 디렉토리가 존재하지 않습니다." >> "$LOG_FILE"
fi

echo ">>> 기존 배포 디렉토리 정리 시작" >> "$LOG_FILE"
if [ -d "$DEPLOY_PATH" ]; then
    # 'scripts' 디렉토리와 'appspec.yml' 파일을 제외하고 삭제
    find "$DEPLOY_PATH" -mindepth 1 ! -path "$DEPLOY_PATH/scripts" ! -name "appspec.yml" -exec rm -rf {} + 2>> "$ERR_LOG_FILE"
    echo ">>> 기존 배포 디렉토리 정리 완료" >> "$LOG_FILE"
else
    echo ">>> DEPLOY_PATH 디렉토리가 존재하지 않습니다: $DEPLOY_PATH" >> "$LOG_FILE"
fi

echo ">>> cleanup.sh 스크립트 완료" >> "$LOG_FILE"

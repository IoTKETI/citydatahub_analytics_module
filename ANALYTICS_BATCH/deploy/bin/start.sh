BIN_PATH=$(dirname $(realpath $0))

cd $BIN_PATH/../

nohup java -jar $BIN_PATH/../apps/AnalyticsBatch-0.0.1-SNAPSHOT.jar -Dspring.config.location=file:$BIN_PATH/../ 1> /dev/null 2>&1 &



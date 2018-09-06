### binlogServer

一款监听 `canal` 发送变更数据到 `kafka` 的中间件




---


### Dockerfile

```docker
FROM docker.today36524.com.cn:5000/base/openjdk:server-jre8
MAINTAINER maple leihuazhe@gmail.com

RUN mkdir -p  /opt/binlog

COPY  ./apps/kafka-binlog-1.0-jar-with-dependencies.jar  /opt/binlog/
COPY  ./startup.sh /opt/binlog/

WORKDIR /opt/binlog
ENTRYPOINT exec  /opt/binlog/startup.sh

# CMD ["sh",  "-c", "/opt/binlog/startup.sh && tail -F /opt/binlog/startup.sh"]
```
#### 启动脚本
```sh
#!/bin/sh
echo "===== begin to startup binlog server ===="
export JVM_HOME='opt/oracle-server-jre'
export PATH=$JVM_HOME/bin:$PATH

# program name
PRO_NAME=binlog-server

# date now like 20180906132800
NOW_DATE=`date +%Y%m%d%H%M%S`


# get the current dir 
PRO_DIR=`pwd`
dirname $0|grep "^/" >/dev/null
if [ $? -eq 0 ];then
   PRO_DIR=`dirname $0`
else
    dirname $0|grep "^\." >/dev/null
    retval=$?
    if [ $retval -eq 0 ];then
        PRO_DIR=`dirname $0|sed "s#^.#$PRO_DIR#"`
    else
        PRO_DIR=`dirname $0|sed "s#^#$PRO_DIR/#"`
    fi
fi


# logger dir 
LOG_DIR=$PRO_DIR/logs
if [ ! -d "$LOG_DIR" ]; then
        mkdir "$LOG_DIR"
fi


# jmx parameter
JMX="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1091 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

# jvm parameter
JVM_OPTS="-Dfile.encoding=UTF-8 -Dsun.jun.encoding=UTF-8 -Dname=$PRO_NAME -Dio.netty.leakDetectionLevel=advanced -Xms512M -Xmx1024M -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -Xloggc:$LOG_DIR/gc-$PRO_NAME-$NOW_DATE.log -XX:+PrintGCDetails -XX:NewRatio=1 -XX:SurvivorRatio=30 -XX:+UseParallelGC -XX:+UseParallelOldGC -Dlog.dir=$PRO_DIR/.."

# soa base
SOA_BASE="-Dsoa.base=$PRO_DIR/../ -Dsoa.run.mode=native"


# SIGTERM  graceful-shutdown
pid=0
process_exit() {
 if [ $pid -ne 0 ]; then
  echo "graceful shutdown pid: $pid" > $LOG_DIR/pid.txt
  kill -SIGTERM "$pid"
  wait "$pid"
 fi
 exit 143; # 128 + 15 -- SIGTERM
}


trap 'kill ${!};process_exit' SIGTERM

nohup java -server $JVM_OPTS $SOA_BASE $DEBUG_OPTS $USER_OPTS  $E_JAVA_OPTS -jar $PRO_DIR/kafka-binlog-1.0-jar-with-dependencies.jar >> $LOG_DIR/console.log 2>&1 &
pid="$!"
echo "start pid: $pid" > $LOG_DIR/pid.txt


wait $pid

```






##### 打包为可执行 jar
```
mvn package assembly:single 
```


FROM docker.today36524.com.cn:5000/base/openjdk:server-jre8
MAINTAINER maple leihuazhe@gmail.com

RUN mkdir -p  /opt/binlog

COPY  ./apps/kafka-binlog-jar-with-dependencies.jar  /opt/binlog/
COPY  ./startup.sh /opt/binlog/

WORKDIR /opt/binlog
ENTRYPOINT exec  /opt/binlog/startup.sh

# CMD ["sh",  "-c", "/opt/binlog/startup.sh && tail -F /opt/binlog/startup.sh"]

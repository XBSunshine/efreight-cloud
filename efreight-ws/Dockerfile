FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-ws

WORKDIR /efreight-ws

EXPOSE 8085

ADD ./efreight-ws/target/efreight-ws.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-ws.jar

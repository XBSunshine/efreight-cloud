FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-config

WORKDIR /efreight-config

EXPOSE 8888

ADD ./efreight-config/target/efreight-config.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-config.jar

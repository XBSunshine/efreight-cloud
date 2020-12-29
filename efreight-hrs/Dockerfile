FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-hrs

WORKDIR /efreight-hrs

EXPOSE 8081

ADD ./efreight-hrs/target/efreight-hrs.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-hrs.jar

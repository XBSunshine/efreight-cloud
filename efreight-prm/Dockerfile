FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-prm

WORKDIR /efreight-prm

EXPOSE 8082

ADD ./efreight-prm/target/efreight-prm.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-prm.jar

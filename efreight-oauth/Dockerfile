FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-oauth

WORKDIR /efreight-oauth

EXPOSE 8080

ADD ./efreight-oauth/target/efreight-oauth.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-oauth.jar

FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER ithongweizhang@gmail.com

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /efreight-eureka

WORKDIR /efreight-eureka

EXPOSE 9001

ADD ./efreight-eureka/target/efreight-eureka.jar ./

CMD java -Djava.security.egd=file:/dev/./urandom -jar efreight-eureka.jar

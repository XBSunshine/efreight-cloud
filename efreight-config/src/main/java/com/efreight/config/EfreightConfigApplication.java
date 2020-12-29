package com.efreight.config;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
@EnableConfigServer
@SpringCloudApplication
public class EfreightConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfreightConfigApplication.class, args);
	}

}

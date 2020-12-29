package com.efreight.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;

@SpringCloudApplication
public class EfreightGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfreightGatewayApplication.class, args);
	}

}

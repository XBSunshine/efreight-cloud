package com.efreight;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

import com.efreight.common.security.annotation.EnableEftFeignClients;


@SpringCloudApplication
@EnableEftFeignClients
public class EfreightOauthApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfreightOauthApplication.class, args);
	}

}

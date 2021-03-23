package com.efreight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.SpringCloudApplication;

import com.efreight.afbase.utils.SpringUtil;
import com.efreight.common.security.annotation.EnableEftFeignClients;
import com.efreight.common.security.annotation.EnableEftResourceServer;

import org.springframework.context.annotation.Import;


@SpringCloudApplication
@EnableEftFeignClients
@EnableEftResourceServer
@ServletComponentScan
@Import(SpringUtil.class)
public class EfreightAfbaseApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(EfreightAfbaseApplication.class, args);
	}

}

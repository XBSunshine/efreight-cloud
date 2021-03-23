package com.efreight;

import cn.hutool.core.util.StrUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.SpringCloudApplication;

import com.efreight.afbase.utils.SpringUtil;
import com.efreight.common.security.annotation.EnableEftFeignClients;
import com.efreight.common.security.annotation.EnableEftResourceServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@SpringCloudApplication
@EnableEftFeignClients
@EnableEftResourceServer
public class EfreightAfbaseApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(EfreightAfbaseApplication.class, args);
	}

	/***
	 * 日期参数接收转换器，将json字符串转为日期类型
	 * @return
	 */
	@Bean
	public Converter<String, LocalDateTime> LocalDateTimeConvert() {
		return new Converter<String, LocalDateTime>() {
			@Override
			public LocalDateTime convert(String source) {
				LocalDateTime date = null;
				if (StrUtil.isNotBlank(source)) {
					DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					try {
						date = LocalDateTime.parse((String) source, df);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return date;
			}
		};
	}
	@Bean
	public Converter<String, LocalDate> LocalDateConvert() {
		return new Converter<String, LocalDate>() {
			@Override
			public LocalDate convert(String source) {
				LocalDate date = null;
				if (StrUtil.isNotBlank(source)) {
					DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					try {
						date = LocalDate.parse((String) source, df);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return date;
			}
		};
	}
}

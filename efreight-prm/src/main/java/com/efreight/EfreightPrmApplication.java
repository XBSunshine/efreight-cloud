package com.efreight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import cn.hutool.core.util.StrUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.efreight.common.security.annotation.EnableEftFeignClients;
import com.efreight.common.security.annotation.EnableEftResourceServer;
import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@SpringCloudApplication
@EnableEftFeignClients
@EnableEftResourceServer
@MapperScan("com.efreight.prm.dao")
@ComponentScan(basePackages = {"com.efreight", "com.efreight.common.core.cache","com.efreight.common.security"})
public class EfreightPrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfreightPrmApplication.class, args);
	}

	@Bean
	public PageHelper pageHelper() {
	    PageHelper pageHelper = new PageHelper();
	    Properties properties = new Properties();
	    properties.setProperty("offsetAsPageNum", "true");
	    properties.setProperty("rowBoundsWithCount", "true");
	    properties.setProperty("reasonable", "true");
	    properties.setProperty("dialect", "mysql");    //配置mysql数据库的方言
	    pageHelper.setProperties(properties);
	    return pageHelper;
	}
	@Configuration
	public class MappingConverterAdapter {
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
	}
}

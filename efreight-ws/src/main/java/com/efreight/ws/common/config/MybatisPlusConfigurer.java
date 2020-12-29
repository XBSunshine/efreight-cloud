
package com.efreight.ws.common.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.efreight.ws.afbase.mapper", "com.efreight.ws.hrs.mapper", "com.efreight.ws.prm.mapper", })
public class MybatisPlusConfigurer {
    /**
     * 配置分页
     * @return
     */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		return new PaginationInterceptor();
	}

    /**
     * 配置逻辑删除
     * @return
     */
	@Bean
	public ISqlInjector sqlInjector() {
		return new LogicSqlInjector();
	}
}

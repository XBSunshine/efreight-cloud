
package com.efreight.afbase.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhanghw
 */
@Configuration
@MapperScan({"com.efreight.afbase.dao","com.efreight.afbase.mapper"})
public class MybatisPlusConfigurer {
	/**
	 * 分页插件
	 *
	 * @return PaginationInterceptor
	 */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		return new PaginationInterceptor();
	}

//	/**
//	 * 数据权限插件
//	 *
//	 * @return DataScopeInterceptor
//	 */
//	@Bean
//	public DataScopeInterceptor dataScopeInterceptor() {
//		return new DataScopeInterceptor();
//	}

	/**
	 * 逻辑删除
	 *
	 * @return
	 */
	@Bean
	public ISqlInjector sqlInjector() {
		return new LogicSqlInjector();
	}
}


package com.efreight.common.security.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

import com.efreight.common.security.component.EftResourceServerAutoConfiguration;
import com.efreight.common.security.component.EftSecurityBeanDefinitionRegistrar;

import java.lang.annotation.*;

/**
 * @author zhanghw
 */
@Documented
@Inherited
@EnableResourceServer
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Import({EftResourceServerAutoConfiguration.class, EftSecurityBeanDefinitionRegistrar.class })
public @interface EnableEftResourceServer {

}


package com.efreight.oauth.config;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.component.EftWebResponseExceptionTranslator;
import com.efreight.common.security.service.ClientDetailsService;
import com.efreight.common.security.service.EUserDetails;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author zhanghw
 *
 */

@Configuration
@AllArgsConstructor
@EnableAuthorizationServer

public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	private final DataSource dataSource;
	private final UserDetailsService userDetailsService;
	private final AuthenticationManager authenticationManagerBean;
	private final RedisConnectionFactory redisConnectionFactory;

	
	
	@Override
	@SneakyThrows
	public void configure(ClientDetailsServiceConfigurer clients) {
		ClientDetailsService clientDetailsService = new ClientDetailsService(dataSource);
		clientDetailsService.setSelectClientDetailsSql(SecurityConstants.DEFAULT_SELECT_STATEMENT);
		clientDetailsService.setFindClientDetailsSql(SecurityConstants.DEFAULT_FIND_STATEMENT);

		clients.withClientDetails(clientDetailsService);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
		oauthServer.allowFormAuthenticationForClients().checkTokenAccess("permitAll()");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST).tokenStore(tokenStore())
				.tokenEnhancer(tokenEnhancer()).userDetailsService(userDetailsService)
				.authenticationManager(authenticationManagerBean).reuseRefreshTokens(false)
				.exceptionTranslator(new EftWebResponseExceptionTranslator());
	}

	@Bean
	public TokenStore tokenStore() {
		RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
		tokenStore.setPrefix(SecurityConstants.PROJECT_PREFIX + SecurityConstants.OAUTH_PREFIX);
		return tokenStore;
	}

	@Bean
	public TokenEnhancer tokenEnhancer() {
		return (accessToken, authentication) -> {
			if (SecurityConstants.CLIENT_CREDENTIALS.equals(authentication.getOAuth2Request().getGrantType())) {
				return accessToken;
			}
			final Map<String, Object> additionalInfo = new HashMap<>(1);
			EUserDetails eUser = (EUserDetails) authentication.getUserAuthentication().getPrincipal();
			additionalInfo.put("userId", eUser.getId());
			additionalInfo.put("userCname", eUser.getUserCname());
			additionalInfo.put("userEname", eUser.getUserEname());
			additionalInfo.put("userName", eUser.getUsername());
			additionalInfo.put("deptId", eUser.getDeptId());
			additionalInfo.put("orgId", eUser.getOrgId());
			additionalInfo.put("userEmail", eUser.getUserEmail());
            additionalInfo.put("phoneNumber", eUser.getPhoneNumber());
            additionalInfo.put("orderEditNewPage", eUser.getOrderEditNewPage());
            additionalInfo.put("orderSaveClosePage", eUser.getOrderSaveClosePage());
			additionalInfo.put("license", SecurityConstants.PROJECT_LICENSE);
			((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
			return accessToken;
		};
	}

}

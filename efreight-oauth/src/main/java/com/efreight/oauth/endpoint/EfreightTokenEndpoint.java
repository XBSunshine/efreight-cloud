package com.efreight.oauth.endpoint;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.util.MessageInfo;

import lombok.AllArgsConstructor;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zhanghw
 */
@RestController
@AllArgsConstructor
@RequestMapping("/token")
public class EfreightTokenEndpoint {
	private static final String PROJECT_OAUTH_ACCESS = SecurityConstants.PROJECT_PREFIX + SecurityConstants.OAUTH_PREFIX
			+ "access:";
	private static final String CURRENT = "current";
	private static final String SIZE = "size";
	private final TokenStore tokenStore;
	private final RedisTemplate eftRedisTemplate;
	private final ClientDetailsService clientDetailsService;
	private final CacheManager cacheManager;

	/**
	 * 退出并删除token
	 *
	 * @param authHeader Authorization
	 */
	@DeleteMapping("/logout")
	public MessageInfo<Boolean> logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		if (StrUtil.isBlank(authHeader)) {
			return MessageInfo.ok(Boolean.FALSE, "退出失败，token 为空");
		}

		String tokenValue = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
		if (accessToken == null || StrUtil.isBlank(accessToken.getValue())) {
			return MessageInfo.ok(Boolean.TRUE, "退出失败，token 无效");
		}

		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(accessToken);
		// 清空用户信息
		cacheManager.getCache("user_details").evict(auth2Authentication.getName());
		cacheManager.getCache("user_details").clear();
		// 清空access token
		tokenStore.removeAccessToken(accessToken);

		// 清空 refresh token
		OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
		tokenStore.removeRefreshToken(refreshToken);
		return MessageInfo.ok(Boolean.TRUE);
	}

	/**
	 * 令牌管理调用
	 *
	 * @param token token
	 * @param from  内部调用标志
	 */
	@DeleteMapping("/{token}")
	public MessageInfo<Boolean> removeToken(@PathVariable("token") String token,
			@RequestHeader(required = false) String from) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
		tokenStore.removeAccessToken(oAuth2AccessToken);
		return new MessageInfo<>();
	}

	/**
	 * 查询token
	 *
	 * @param params 分页参数
	 * @param from   标志
	 */
	@PostMapping("/page")
	public MessageInfo getTokenPage(@RequestBody Map<String, Object> params,
			@RequestHeader(required = false) String from) {
		// 根据分页参数获取对应数据
		String key = String.format("%s*:%s", PROJECT_OAUTH_ACCESS);
		List<String> pages = findKeysForPage(key, MapUtil.getInt(params, "current"),
				MapUtil.getInt(params, "size"));

		eftRedisTemplate.setKeySerializer(new StringRedisSerializer());
		eftRedisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		Page result = new Page(MapUtil.getInt(params, "current"),
				MapUtil.getInt(params, "size"));
		result.setRecords(eftRedisTemplate.opsForValue().multiGet(pages));
		result.setTotal(Long.valueOf(eftRedisTemplate.keys(key).size()));
		return MessageInfo.ok(result);
	}

	private List<String> findKeysForPage(String patternKey, int pageNum, int pageSize) {
		ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
		RedisSerializer<String> redisSerializer = (RedisSerializer<String>) eftRedisTemplate.getKeySerializer();
		Cursor cursor = (Cursor) eftRedisTemplate.executeWithStickyConnection(
				redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
		List<String> result = new ArrayList<>();
		int tmpIndex = 0;
		int startIndex = (pageNum - 1) * pageSize;
		int end = pageNum * pageSize;

		assert cursor != null;
		while (cursor.hasNext()) {
			if (tmpIndex >= startIndex && tmpIndex < end) {
				result.add(cursor.next().toString());
				tmpIndex++;
				continue;
			}
			if (tmpIndex >= end) {
				break;
			}
			tmpIndex++;
			cursor.next();
		}
		return result;
	}
}

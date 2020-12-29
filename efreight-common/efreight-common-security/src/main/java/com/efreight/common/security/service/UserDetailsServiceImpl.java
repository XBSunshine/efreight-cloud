
package com.efreight.common.security.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import com.efreight.common.security.constant.CommonConstants;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.exception.EftAuth2Exception;

import com.efreight.common.security.feign.RemoteUserService;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.vo.UserInfo;
import com.efreight.common.security.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户详细信息
 *
 * @author zhanghw
 */
@Slf4j
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final RemoteUserService remoteUserService;
    private final CacheManager cacheManager;

    /**
     * 用户密码登录
     *
     * @param username 用户名
     * @return
     */
    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String username) {
        String[] users = username.split("\\|");
        if (users.length != 3) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Cache cache = cacheManager.getCache("user_details");
        if (cache != null && cache.get(username) != null) {
            log.info("结果：" + cache.get(username).get());
            return (UserDetails) cache.get(username).get();
        }
        MessageInfo result = remoteUserService.info(username, SecurityConstants.FROM_IN);
        log.info("用户结果：" + result);
        if (result.getCode() != 0)
            throw new EftAuth2Exception(result.getMessageInfo(), "" + result.getCode());
        UserDetails userDetails = getUserDetails(result, users[2]);
        cache.put(username, userDetails);
        return userDetails;
    }

    /**
     * 构建userdetails
     *
     * @param result 用户信息
     * @return
     */
    private UserDetails getUserDetails(MessageInfo<UserInfo> result, String orgCode) throws ParseException, IOException {
        if (result == null || result.getData() == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        UserInfo info = result.getData();

        Set<String> dbAuthsSet = new HashSet<>();

        if (ArrayUtil.isNotEmpty(info.getRoles())) {
            // 获取角色
            Arrays.stream(info.getRoles()).forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role));
            // 获取资源
            dbAuthsSet.addAll(Arrays.asList(info.getPermissions()));

        }
        Collection<? extends GrantedAuthority> authorities = AuthorityUtils
                .createAuthorityList(dbAuthsSet.toArray(new String[0]));

        UserVo user = info.getUserVo();
        // 构造security用户
        EUserDetails userDetails =  new EUserDetails(user.getUserId(), user.getDeptId(), user.getOrgId(), orgCode + "_" + user.getLoginName(),
                SecurityConstants.BCRYPT + user.getPassWord(), user.getUserEmail(), user.getUserName(), user.getUserEname(), StrUtil.equals("0", CommonConstants.STATUS_NORMAL), true,
                true, true, authorities);
        userDetails.setPhoneNumber(user.getPhoneNumber());
        userDetails.setOrderEditNewPage(user.getOrderEditNewPage());
        userDetails.setOrderSaveClosePage(user.getOrderSaveClosePage());
        return userDetails;
    }

}

package com.efreight.common.security.component;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.StringUtils;

import com.efreight.common.security.service.EUserDetails;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhanghw
 * 根据checktoken 的结果转化用户信息
 */
public class EftUserAuthenticationConverter implements UserAuthenticationConverter {
    private static final String USER_ID = "userId";
    private static final String DEPT_ID = "deptId";
    private static final String ORG_ID = "orgId";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_CNAME = "userCname";
    private static final String USER_ENAME = "userEname";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String N_A = "N/A";

    /**
     * Extract information about the user to be used in an access token (i.e. for resource servers).
     *
     * @param authentication an authentication representing a user
     * @return a map of key values representing the unique information about the user
     */
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(USERNAME, authentication.getName());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    /**
     * Inverse of {@link #convertUserAuthentication(Authentication)}. Extracts an Authentication from a map.
     *
     * @param map a map of user information
     * @return an Authentication representing the user or null if there is none
     */
    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
            String username = (String) map.get(USERNAME);
            Integer id = (Integer) map.get(USER_ID);
            Integer deptId = (Integer) map.get(DEPT_ID);
            Integer orgId = (Integer) map.get(ORG_ID);
            String userEmail = (String) map.get(USER_EMAIL);
            String userCname = (String) map.get(USER_CNAME);
            String userEname = (String) map.get(USER_ENAME);
            String userPhone = (String) map.get(PHONE_NUMBER);
            EUserDetails user = new EUserDetails(id, deptId, orgId, username, N_A, userEmail, userCname, userEname, true
                    , true, true, true, authorities);
            user.setPhoneNumber(userPhone);
            return new UsernamePasswordAuthenticationToken(user, N_A, authorities);
        }
        return null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}

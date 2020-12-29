
package com.efreight.common.security.feign;

import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.constant.ServiceNameConstants;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.vo.UserInfo;
import com.efreight.common.security.vo.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author zhanghw
 * 
 */
@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.HRS_SERVICE)
public interface RemoteUserService {
	/**
	 * 通过用户名查询用户、角色信息
	 *
	 * @param username 用户名
	 * @param from     调用标志
	 * @return R
	 */
	@GetMapping("/user/info/{username}")
	MessageInfo<UserInfo> info(@PathVariable("username") String username,
							   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 通过社交账号查询用户、角色信息
	 *
	 * @param inStr appid@code
	 * @return
	 */
	@GetMapping("/social/info/{inStr}")
	MessageInfo<UserInfo> social(@PathVariable("inStr") String inStr);

	/**
	 * 获取签约公司管理员
	 * @param orgId
	 * @return
	 */
	@GetMapping("/user/queryAdminByOrgId/{orgId}")
	MessageInfo<UserVo> queryAdminByOrgId(@PathVariable("orgId") Integer orgId);

	/**
	 * 通过userId查询用户
	 * @param id
	 * @return
	 */
	@GetMapping("/user/{id}")
	MessageInfo<UserVo> user(@PathVariable("id") Integer id);
}

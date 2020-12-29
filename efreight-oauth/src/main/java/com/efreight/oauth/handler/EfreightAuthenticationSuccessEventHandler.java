
package com.efreight.oauth.handler;

import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.handler.AbstractAuthenticationSuccessEventHandler;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.vo.LogVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zhanghw
 */
@Slf4j
@Component
@AllArgsConstructor
public class EfreightAuthenticationSuccessEventHandler extends AbstractAuthenticationSuccessEventHandler {

	private final RemoteServiceToHRS remoteServiceToHRS;
	/**
	 * 处理登录成功方法
	 * <p>
	 * 获取到登录的authentication 对象
	 *
	 * @param authentication 登录对象
	 */
	@Override
	public void handle(Authentication authentication) {
		log.info("用户：{} 登录成功", authentication.getPrincipal());
		try{
			recordLoginLog(authentication);
		}catch (Exception e){
			log.error("登录日志记录失败", e);
		}
	}

	private void recordLoginLog(Authentication authentication){
		Object principal = authentication.getPrincipal();
		Object details = authentication.getDetails();
		if(principal instanceof EUserDetails && details instanceof Map){
			EUserDetails userDetails = (EUserDetails) principal;
			Map<String, String> detailsMap = (Map)authentication.getDetails();

			LogVo logVo = new LogVo();
			logVo.setOpType("登录");
			logVo.setOpName("生态云登录");
			logVo.setOpLevel("高");
			logVo.setOpInfo(detailsMap.get("loginType"));
			logVo.setCreatorId(userDetails.getId());
			logVo.setOrgId(userDetails.getOrgId());
			logVo.setDeptId(userDetails.getDeptId());
			remoteServiceToHRS.recordLog(logVo, SecurityConstants.FROM_IN);
		}else{
			log.warn("Unknown type, unable to record login log.");
		}
	}
}

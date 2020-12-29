package com.efreight.hrs.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.service.SourceLoginOfEfService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@RequestMapping("/ef")
@Slf4j
public class EfController {
	private final SourceLoginOfEfService sourceLoginOfEfService;
    /**
     * 通行证修改密码
     * @param phoneArea
     * @param phone
     * @param oldPwd
     * @param newPwd
     * @param orgId
     * @return
     */
    @PostMapping(value = "/sourceUserOfEfPwd")
    public MessageInfo sourceUserOfEfPwd(String phoneArea,String phone,String oldPwd,String newPwd,Integer orgId) {
    	try {
    		if(StringUtils.isEmpty(phoneArea)) {
	    		return MessageInfo.failed("手机区号为空");
			}
	        if(StringUtils.isEmpty(phone)) {
	        	return MessageInfo.failed("手机号为空");
			}
	        if(orgId==null||orgId==0) {
	        	return MessageInfo.failed("签约公司id为空或无效");
	        }
//	        if(StringUtils.isEmpty(oldPwd)) {
//	        	return MessageInfo.failed("旧密码为空");
//	        }
	        if(StringUtils.isEmpty(newPwd)) {
	        	return MessageInfo.failed("新密码为空");
	        }
    	  sourceLoginOfEfService.sourceUserOfEfPwd(phoneArea,phone,oldPwd,newPwd,orgId);
    	 return MessageInfo.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return MessageInfo.failed(e.getMessage());
		}
    }
    /**
     * 通行证修改邮箱或补录邮箱
     * @param phoneArea
     * @param phone
     * @param oldPwd
     * @param newPwd
     * @param orgId
     * @return
     */
    @PostMapping(value = "/sourceUserOfEfEmail")
    public MessageInfo sourceUserOfEfEmail(String phoneArea,String phone,String email,Integer orgId) {
    	try {
//    		,String userName
    		if(StringUtils.isEmpty(phoneArea)) {
	    		return MessageInfo.failed("手机区号为空");
			}
	        if(StringUtils.isEmpty(phone)) {
	        	return MessageInfo.failed("手机号为空");
			}
	        if(orgId==null||orgId==0) {
	        	return MessageInfo.failed("签约公司id为空或无效");
	        }
	        if(StringUtils.isEmpty(email)) {
	        	return MessageInfo.failed("邮箱为空");
	        }
	       
    	  sourceLoginOfEfService.sourceUserOfEfEmail(phoneArea,phone,email.toLowerCase(),orgId);
    	 return MessageInfo.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return MessageInfo.failed(e.getMessage());
		}
    }
    /**
     * 通行证修改用户昵称 企业级
     * @param phoneArea
     * @param phone
     * @param oldPwd
     * @param newPwd
     * @param orgId
     * @return
     */
    @PostMapping(value = "/efUserName")
    public MessageInfo efUserName(String phoneArea,String phone,String userName,Integer orgId) {
    	try {
//    		,String userName
    		if(StringUtils.isEmpty(phoneArea)) {
	    		return MessageInfo.failed("手机区号为空");
			}
	        if(StringUtils.isEmpty(phone)) {
	        	return MessageInfo.failed("手机号为空");
			}
	        if(orgId==null||orgId==0) {
	        	return MessageInfo.failed("签约公司id为空或无效");
	        }
	        if(StringUtils.isEmpty(userName)) {
	        	return MessageInfo.failed("参数用户名称为空");
	        }
	       
    	  sourceLoginOfEfService.efUserName(phoneArea,phone,userName.toLowerCase(),orgId);
    	 return MessageInfo.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return MessageInfo.failed(e.getMessage());
		}
    }
}

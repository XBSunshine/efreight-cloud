package com.efreight.hrs.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * ef官网登录生态云服务接口
 * @author cwd
 *
 */
public interface SourceLoginOfEfService{
	Map getToken(String token);
	boolean sourceUserOfEfPwd(String phoneArea,String phone,String oldPwd,String newPwd,Integer orgId);
	boolean sourceUserOfEfEmail(String phoneArea,String phone,String email,Integer orgId);
	boolean efUserName(String phoneArea,String phone,String userName,Integer orgId);
}

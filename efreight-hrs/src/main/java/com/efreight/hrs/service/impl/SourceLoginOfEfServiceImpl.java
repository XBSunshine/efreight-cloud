package com.efreight.hrs.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.efreight.hrs.dao.OrgMapper;
import com.efreight.hrs.dao.UserMapper;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.User;
import com.efreight.hrs.service.SourceLoginOfEfService;
import com.efreight.hrs.utils.HttpConnectionUtil;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class SourceLoginOfEfServiceImpl  implements SourceLoginOfEfService{
	 
	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
	@Autowired
    UserMapper userMapper;
	@Autowired
    OrgMapper orgMapper;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	LoadBalancerClient loadBalancerClient;
	@Autowired
	CacheManager cacheManager;
	@Value("${dnspath}")
	private String dnspath;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map getToken(String token) {
		String msg = "";
		Map map = new HashMap();
		String url = "";
		String yu = "";
		try {
			System.out.println(dnspath);
			//根据token 校验token合法性  如果合法获取相应用户信息
			if(dnspath.contains("tom.efreight.cn")) {
				url = "http://pass.yctop.com/usr?token=";
				yu = "www.efreight.cn";
			}else {
				url = "http://uc.yctop.com/usr?token=";
				yu = "dev.efreight.cn";
			}
			String result = HttpConnectionUtil.sendByGet(url+token);
			if(StringUtils.isEmpty(result)) {
				msg = "msg-用户信息存在异常无法访问，请联系管理员";
				throw new RuntimeException(msg);
			}
			  JSONObject jsonO = JSONObject.parseObject(result);
			if(!"200".equals(jsonO.getString("code"))||jsonO.get("data")==null) {
				msg = "msg-用户信息异常无法访问";
				throw new RuntimeException(msg);
			}
			JSONObject jsonData = JSONObject.parseObject(jsonO.getString("data"));
			JSONObject jsonUserInfo = JSONObject.parseObject(jsonData.getString("userInfo"));
			Integer orgId = Integer.valueOf(jsonUserInfo.getString("orgId")).intValue();
			String  email = jsonUserInfo.getString("email");
			String  loginName = jsonUserInfo.getString("loginNameSaaS");
			String  phoneArea = jsonUserInfo.getString("area");
			if("86".equals(phoneArea)) {//大陆
	    		phoneArea = "00"+phoneArea;
	    	}else {
	    		//香港852
	        	//澳门853
	        	//台湾886
	    		phoneArea = "0"+phoneArea;
	    	}
			String  phone = jsonUserInfo.getString("phone");
			//查询用户信息进行校验
			LambdaQueryWrapper<User> userWrapperP = Wrappers.<User>lambdaQuery();
		    userWrapperP.eq(User::getIsadmin, 0).eq(User::getLoginName, loginName).eq(User::getPhoneNumber, phone).eq(User::getOrgId, orgId);
		    User userWrapperPInfo= userMapper.selectOne(userWrapperP);
            String code = "";
            String name = "";
            String orgVersion = "";
            boolean orderFinanceLockView = false;
            Integer orgType  = null;
			if(userWrapperPInfo==null) {
			  msg = "msg-用户信息异常无法访问";
			  throw new RuntimeException(msg);
			}else {
			  //取签约公司信息
		      Org org = orgMapper.selectById(userWrapperPInfo.getOrgId());
		      map.put("loginName", "0|"+userWrapperPInfo.getLoginName()+"|"+org.getOrgCode());
		      code = org.getOrgCode();
		      name = org.getOrgName();
		      orgType = org.getOrgType();
		      orderFinanceLockView = org.isOrderFinanceLockView();
		      orgVersion = orgMapper.selectById(org.getOrgEditionId()).getOrgName();
			}
			String str =  new String(Base64Utils.decode(userWrapperPInfo.getPassWordVerification().getBytes()));
//	        str = str.substring(str.indexOf(phoneArea)+phoneArea.length(),str.indexOf(phone));
//	        map.put("verification","" );
//	        map.put("loginName", "0|161715@ef.com|REGIST-259");
//	        map.put("verification", "12345678");
//	        map.put("orgCode", "REGIST-259");
			//根据用户信息 解码 模拟登录
			map = this.postToken(map.get("loginName").toString(),str);
			if(map!=null&&map.containsKey("access_token")) {
				map.put("source", yu);
				map.put("orgCode", code);
				map.put("orgName", name);
				map.put("orgVersion", orgVersion);
				map.put("orgType", orgType);
				map.put("orderFinanceLockView", orderFinanceLockView);
				map.put("userEmail", userWrapperPInfo.getUserEmail()==null?"":userWrapperPInfo.getUserEmail());
			}else {
				 msg = "msg-用户信息异常无法访问";
				throw new RuntimeException(msg);
			}
//			map = this.postToken("0|008633333|REGIST-280","12345678");
//			if(map!=null&&map.containsKey("access_token")) {
//				map.put("source", yu);
//				map.put("orgCode", "1111");
//				map.put("orgName", "22222");
//			}else {
//				 msg = "msg-用户信息异常无法访问";
//				throw new RuntimeException(msg);
//			}
			//返回前端token等相关信息
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return map;
	}
	
	/*
	 * 模拟登录申请令牌
	 */
	private Map postToken(String userName,String password) {
	
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic("efreight","efreight");
        header.add("isToken","false");
        header.add("Authorization",httpBasic);
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",userName);
        body.add("password",password);
        body.add("client_id","efreight");
        body.add("client_secret","efreight");
        
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.postForEntity("http://efreight-oauth/oauth/token", httpEntity, Map.class);
        //申请令牌信息
        Map bodyMap = exchange.getBody();
       
        return bodyMap;
		
	}

	//获取httpbasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

	@Override
	public boolean sourceUserOfEfPwd(String phoneArea, String phone, String oldPwd, String newPwd, Integer orgId) {
		if("86".equals(phoneArea)) {//大陆
    		phoneArea = "00"+phoneArea;
    	}else {
    		//香港852
        	//澳门853
        	//台湾886
    		phoneArea = "0"+phoneArea;
    	}
		//根据传入参数查询用户
		User user = userMapper.getUserInfoByUserPhone(phone, phoneArea, orgId, null);
		if (user == null) {
            throw new RuntimeException("用户不存在");
        }
//		String old = new String(Base64Utils.encode((oldPwd).getBytes()));
//		if(!old.equals(user.getPassWordVerification())) {
//			throw new RuntimeException("旧密码错误");
//		}
		User upUser = new User();
		upUser.setUserId(user.getUserId());
		upUser.setEditorId(user.getUserId());
		upUser.setEditTime(LocalDateTime.now());
		upUser.setPassWord(ENCODER.encode(newPwd));
		upUser.setPassWordVerification(new String(Base64Utils.encode((newPwd).getBytes())));
		userMapper.updateById(upUser);
		return true;
	}

	@Override
	public boolean sourceUserOfEfEmail(String phoneArea, String phone, String email, Integer orgId) {
		if("86".equals(phoneArea)) {//大陆
    		phoneArea = "00"+phoneArea;
    	}else {
    		//香港852
        	//澳门853
        	//台湾886
    		phoneArea = "0"+phoneArea;
    	}
		//根据传入参数查询用户
		User user = userMapper.getUserInfoByUserPhone(phone, phoneArea, orgId, null);
		if (user == null) {
            throw new RuntimeException("用户不存在");
        }
		Integer count = userMapper.countByUserEmail2(email,user.getUserId());
	    if(count>0){
	        throw new RuntimeException("邮箱已存在");
	    }
	    User upUser = new User();
		upUser.setUserId(user.getUserId());
		upUser.setEditorId(user.getUserId());
		upUser.setEditTime(LocalDateTime.now());
		upUser.setUserEmail(email);
		userMapper.updateById(upUser);
		//查询企业
		Org org = orgMapper.selectById(orgId);
		//清理redis 缓存
		Cache cache = cacheManager.getCache("user_details");
		String userName = "0|"+user.getLoginName()+"|"+org.getOrgCode();
        if (cache != null && cache.get(userName) != null) {
        	cacheManager.getCache("user_details").evict(userName);
//    		cacheManager.getCache("user_details").clear();
        }
		return true;
	}

	@Override
	public boolean efUserName(String phoneArea, String phone, String userName, Integer orgId) {
		//查询该用户所在公司 的版本 体验版
		if("86".equals(phoneArea)) {//大陆
    		phoneArea = "00"+phoneArea;
    	}else {
    		//香港852
        	//澳门853
        	//台湾886
    		phoneArea = "0"+phoneArea;
    	}
		//根据传入参数查询用户
		User user = userMapper.getUserInfoByUserPhone(phone, phoneArea, orgId, null);
		if (user == null) {
            throw new RuntimeException("用户不存在");
        }
		//查询企业信息 以及企业所在的版本  体验版 才修改  其他 忽略
		Org org = orgMapper.getOrgVersion(orgId);
		if(org==null) {
			throw new RuntimeException("所在签约公司或者签约用户不存在");
		}
		if("体验版".equals(org.getOrgName())) {
		   User userUpdate = userMapper.selectById(user.getUserId());
		   userUpdate.setUserName(userName);
		   userUpdate.setUserEname(userName);
		   userMapper.updateById(userUpdate);
		   //查询企业
		   Org orgTwo = orgMapper.selectById(orgId);
		   //清理redis 缓存
		   Cache cache = cacheManager.getCache("user_details");
		   String userNameClear = "0|"+userUpdate.getLoginName()+"|"+orgTwo.getOrgCode();
           if (cache != null && cache.get(userNameClear) != null) {
        	 cacheManager.getCache("user_details").evict(userNameClear);
//    		 cacheManager.getCache("user_details").clear();
           }
		}
		return true;
	}

}

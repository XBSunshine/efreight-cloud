package com.efreight.prm.controller;


import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.LogBean;
import com.efreight.prm.service.LogService;
import com.efreight.prm.util.MessageInfo;



@RestController
@RequestMapping("/log")
public class LogController {

	@Autowired
	private LogService service;
	
	@RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public MessageInfo queryList(Integer currentPage, Integer pageSize,@ModelAttribute("bean") LogBean bean) {
		String message = "success";
		int code =200;
		Map<String,Object> dataMap = new HashMap();
		try{
//			if ( bean.getOrg_id()==null || "".equals( bean.getOrg_id())) {
//				throw new Exception("企业代码不能为空");
//			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Map<String, Object> paramMap=new HashMap<String, Object>();
			paramMap.put("op_name", bean.getOp_name());
			paramMap.put("op_level", bean.getOp_level());
			paramMap.put("creator_name", bean.getCreator_name());
			if (bean.getCreate_time_begin()!=null) {
				paramMap.put("create_time_begin", df.format(bean.getCreate_time_begin())+" 00:00:00");
			}
			if (bean.getCreate_time_end()!=null) {
				paramMap.put("create_time_end", df.format(bean.getCreate_time_end())+" 23:59:59");
			}
			paramMap.put("org_id",SecurityUtils.getUser().getOrgId());
			dataMap = service.queryList(currentPage, pageSize,paramMap);
			
		}catch(Exception e){
			message=e.getMessage();
			code = 400;
		}
		
		MessageInfo messageInfo =new MessageInfo(dataMap,message);
		messageInfo.setCode(code);
        return  messageInfo;
    }
	
}


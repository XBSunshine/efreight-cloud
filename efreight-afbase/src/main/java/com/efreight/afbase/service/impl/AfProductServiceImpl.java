package com.efreight.afbase.service.impl;


import com.efreight.afbase.entity.view.SendProductEmail;
import com.efreight.afbase.service.AfProductService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.OrgVo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class AfProductServiceImpl implements AfProductService{
	
	 private final RemoteServiceToHRS remoteServiceToHRS;
	 private final MailSendService mailSendService;
	
	@Override
	public boolean sendProductEmail(SendProductEmail sendProductEmail) {
		
		try {
			//查询收件人 收件人：翌飞签约公司  org_id = 1， 风控邮箱；
			OrgVo org = remoteServiceToHRS.getByOrgId(1).getData();
			StringBuffer sb = new StringBuffer();
			sb.append("签约公司:").append(sendProductEmail.getCompanyName()).append("<br/>");
			sb.append("申请人:").append(SecurityUtils.getUser().getUserCname()).append(" ").append(SecurityUtils.getUser().getUserEmail()).append("<br>");
			sb.append("产品名称:").append(sendProductEmail.getProductName()).append("<br/>");
			sb.append("产品说明:").append(sendProductEmail.getProductDescribe());
	        ArrayList<Map<String, String>> fileList = new ArrayList<>();
	        HashMap<String, String> fileMap = new HashMap<>();
	        fileMap.put("name", sendProductEmail.getFileName().substring(sendProductEmail.getFileName().lastIndexOf("_")+1,sendProductEmail.getFileName().length()));
	        fileMap.put("path", sendProductEmail.getFilePath());
			fileMap.put("flag", "local");
			fileList.add(fileMap);
            mailSendService.sendAttachmentsMailNew(false, new String[] {org.getRcEmail()}, null, null, "产品发布申请-"+sendProductEmail.getProductName(), sb.toString(), fileList,null);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}

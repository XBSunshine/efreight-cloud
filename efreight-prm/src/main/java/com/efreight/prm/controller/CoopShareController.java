package com.efreight.prm.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.CoopShare;
import com.efreight.prm.entity.CoopShareEmail;
import com.efreight.prm.entity.PrmCoopShareFields;
import com.efreight.prm.service.CoopShareService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 协作公司
 * @author cwd
 *
 */
@RestController
@RequestMapping("coopShare")
@AllArgsConstructor
@Slf4j
public class CoopShareController {
	
	private final CoopShareService coopShareService;
	
    /**
     * 分页查询
     *
     * @param
     * @param CoopShare
     * @return
     */
    @GetMapping("/page")
    public MessageInfo page(Integer size,Integer current, CoopShare coopShare) {
        try {
            Map<String, Object> result = coopShareService.getPage(current, size, coopShare);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 设置为协作公司
     *
     * @param coopId shareType 1/0
     * @return
     */
    @PutMapping("/share/{coopId}/{shareType}")
    public MessageInfo share(@PathVariable Integer coopId,@PathVariable String shareType) {
        try {
        	coopShareService.modifyShare(coopId,shareType);
            return MessageInfo.ok();
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 协作发送邮件
     * @param CoopShareEmail
     * @return
     */
    @PostMapping(value = "/coopShareEmail")
    public MessageInfo coopShareEmail(@RequestBody CoopShareEmail coopShareEmail) {
        try {
        	coopShareEmail.setOperator(SecurityUtils.getUser().getUserCname());
        	coopShareEmail.setOperatorEmail(SecurityUtils.getUser().getUserEmail());
        	coopShareEmail.setOperatorPhone(SecurityUtils.getUser().getPhoneNumber());
        	coopShareService.coopShareWithEmail(coopShareEmail);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 更具 客商资料ID 以及ORGID 查询相关信息
     * @param 
     * @return
     */
    @GetMapping("/coopShareInfo/{orgId}/{coopId}")
    public MessageInfo coopShareInfo(@PathVariable Integer orgId,@PathVariable Integer coopId) {
        try {
            return MessageInfo.ok(coopShareService.getCoopShareInfo(orgId,coopId));
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 绑定协作
     * @param coopShareEmail
     * @return
     */
    @PostMapping(value = "/coopShareBind")
    public MessageInfo coopShareBind(@RequestBody CoopShareEmail coopShareEmail) {
        try {
        	EUserDetails user = SecurityUtils.getUser();
        	coopShareEmail.setOrgId(user.getOrgId());
        	coopShareEmail.setBindTime(new Date());
        	coopShareEmail.setOperTime(new Date());
        	coopShareEmail.setBindUserId(user.getId());
        	coopShareEmail.setBindUserName(user.getUserCname());
        	coopShareEmail.setOperUserId(coopShareEmail.getUserId());
        	coopShareEmail.setOperUserName(coopShareEmail.getUserName());
        	//查询根据userId  查询userName
        	coopShareService.coopShareBind(coopShareEmail);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    /**
     * 获取分享协作公司-绑定设置 数据源
     * @param 
     * @return
     */
    @GetMapping("/sharefields/{businessScope}/{coopId}")
    public MessageInfo sharefieldsInfo(@PathVariable String businessScope,@PathVariable Integer coopId) {
        try {
            return MessageInfo.ok(coopShareService.sharefieldsInfo(businessScope,coopId));
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 
     * @param prmCoopShareFields
     * @return
     */
    @PostMapping(value = "/saveShareFields")
    public MessageInfo saveShareFields(@RequestBody PrmCoopShareFields prmCoopShareFields) {
        try {
        	coopShareService.saveShareFields(prmCoopShareFields);
            return MessageInfo.ok();
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }
    
    /**
     * 新校验  签约公司与签约公司之间只能 绑定一次
     * @param 
     * @return
     */
    @GetMapping("/{orgId}/{coopOrgId}")
    public MessageInfo checkBindNew(@PathVariable Integer orgId,@PathVariable Integer coopOrgId) {
        try {
        	CoopShare coopShare = coopShareService.checkBindNew(orgId,coopOrgId);
        	if(coopShare!=null) {
        		return MessageInfo.ok(false);
        	}else {
        		return MessageInfo.ok(true);
        	}
            
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

}

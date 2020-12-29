package com.efreight.prm.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CoopShareEmail implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//收件人
    private String toUsers;
    //抄送人
    private String ccUsers;
    //邮件标题
    private String subject;
    //邮件内容
    private String content;
    //附加信息（网址)
    private String website;
    //签约公司名
    private String orgName;
    //操作人
    private String operator;
    //操作人邮箱
    private String operatorEmail;
    //操作人电话
    private String operatorPhone;
    public void checkRequired(){
        if(StringUtils.isBlank(this.toUsers)){
            throw new RuntimeException("收件人不能为空");
        }
        if(StringUtils.isBlank(this.subject)){
            throw new RuntimeException("邮件标题不能为空");
        }
        if(StringUtils.isBlank(this.content)){
            throw new RuntimeException("邮件内容不能为空");
        }
    }
    
    /**
     * 协作绑定相关
     */
    //指定绑定的 客商资料ID
    private Integer coopId;
    //请求绑定的orgId
    private String orgUuid;
    //请求绑定的客商资料ID
    private Integer orgCoopid;
    //当前操作绑定的OrgId
    private Integer orgId;
    private String bindUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date bindTime;
    private Integer bindUserId;
    private String operUserName;
    private Integer operUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operTime;
    
    private Integer userId;
    private String userName;
    
}

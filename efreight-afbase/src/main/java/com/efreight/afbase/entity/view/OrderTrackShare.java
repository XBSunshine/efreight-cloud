package com.efreight.afbase.entity.view;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * 订单轨迹分享
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderTrackShare implements Serializable {
    //收件人
    private String toUsers;
    //抄送人
    private String ccUsers;
    //签约公司名
    private String orgName;
    //邮件标题
    private String subject;
    //邮件内容
    private String content;
    //附加信息（网址)
    private String website;
    //图片信息
    private String imageURL;
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
        if(StringUtils.isBlank(this.operator)){
            throw new RuntimeException("操作人不能为空");
        }
    }

    public String[] toUserEmails(){
        return this.parse(this.toUsers, ";");
    }

    public String[] ccUserEmails() {
        return this.parse(this.ccUsers, ";");
    }

    private String[] parse(String str, String delimiter){
        if(StringUtils.isBlank(str)){
            return new String[]{};
        }
        Assert.hasLength(delimiter, "未指定分割符!");
        return str.trim().split(delimiter);
    }
    private String orderShareEndTime;
}

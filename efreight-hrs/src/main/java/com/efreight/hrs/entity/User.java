package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String loginName;

    private String passWord;

    private String jobNumber;

    private String userEmail;

    private String phoneNumber;

    private String userName;

    private String userEname;

    private String idType;

    private String idNumber;

    private String userSex;

    private LocalDateTime userBirthday;

    private LocalDateTime hireDate;

    private String employmentType;

    private String userResume;

    private String userFamily;

    private String userRemarks;

    private String jobPosition;

    private LocalDateTime leaveDate;

    private String leaveReason;

    private LocalDateTime blacklistDate;

    private String blacklistReason;

    private Integer creatorId;

    private LocalDateTime createTime;

    private Integer editorId;

    private LocalDateTime editTime;

    private Integer orgId;

    private Integer deptId;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String roleName;

    private Boolean userStatus;

    private Boolean isadmin;

    @TableField(exist = false)
    private Integer userCount;

    @TableField(exist = false)
    private Integer orgUserCount;
    
    private String internationalCountryCode;
    
    private String passWordVerification;

    private Boolean mailValid;

    //昵称
    private String mailName;

    //发件箱SMTP
    private String mailSmtp;

    //发件人账号
    private String mailUser;

    //发件人地址
    private String mailAddress;

    //发件人邮箱验证编码
    private String mailVerifyCode;

    private Boolean mailSsl;

    private Integer mailPort;

    //邮件签名
    private String mailSignature;

    //配置是否订单编辑时新打开页面
    private Boolean orderEditNewPage;

    //配置订单编辑保存后是否关闭页面
    private Boolean orderSaveClosePage;

    private Integer orderAeDigitsWeight;
    private Integer orderAeDigitsVolume;
    private Integer orderAeDigitsChargeWeight;
    private Integer orderAiDigitsWeight;
    private Integer orderAiDigitsVolume;
    private Integer orderAiDigitsChargeWeight;
    private Integer orderSeDigitsWeight;
    private Integer orderSeDigitsVolume;
    private Integer orderSeDigitsChargeWeight;
    private Integer orderSiDigitsWeight;
    private Integer orderSiDigitsVolume;
    private Integer orderSiDigitsChargeWeight;
    private Integer orderTeDigitsWeight;
    private Integer orderTeDigitsVolume;
    private Integer orderTeDigitsChargeWeight;
    private Integer orderLcDigitsWeight;
    private Integer orderLcDigitsVolume;
    private Integer orderLcDigitsChargeWeight;

    private Integer orderPermission;
}

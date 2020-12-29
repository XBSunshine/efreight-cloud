package com.efreight.common.security.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6460158148385367682L;
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

    private Boolean userStatus;
    private Boolean ifLeave;
    private Boolean ifBlack;
    private String  ifLeave1;
    private String  ifBlack1;
    /**
     * 角色列表
     */
    private List<RoleVo> roleList;
    private List<Integer> roleIds;


    private LocalDateTime userBirthdayStart;
    private LocalDateTime userBirthdayEnd;
    private LocalDateTime hireDateStart;
    private LocalDateTime hireDateEnd;
    private LocalDateTime leaveDateStart;
    private LocalDateTime leaveDateEnd;
    private LocalDateTime blacklistDateStart;
    private LocalDateTime blacklistDateEnd;
    private LocalDateTime editTimeStart;
    private LocalDateTime editTimeEnd;
    private String deptCode;
    private String orgCode;
    private String orgName;
    private String orgVersion;
    private String loginRole;
    private String orgType;

    private String internationalCountryCode;

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

    //邮箱签名
    private String mailSignature;

    private String adminRole;

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

    //签约公司失效时间
    private LocalDateTime stopDate;
    private String columnStrs;

    //默认抄送人
    private List<Integer> orderTrackCcUser;
    private List<Integer> sendGoodsNotifyCcUser;
    private List<Integer> sendBillCcUser;
    private List<Integer> sendInventoryCcUser;

    private Integer orderPermission;

    List<Integer> userWorkgroupDetailList;

    private Integer rountingSign;

    private String rountingSignBusinessProduct;

}

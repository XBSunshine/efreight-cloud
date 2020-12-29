package com.efreight.hrs.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
public class UserExcelForAll implements Serializable {

    private Integer orderId;

    private String userName;

    private String userEname;

    private String userEmail;

    private String phoneNumber;

    private String idType;

    private String idNumber;

    private String userSex;

    private Date userBirthday;

    private Date hireDate;

    private String employmentType;

    private String jobPosition;

    private Date leaveDate;

    private String leaveReason;

    private Date blacklistDate;

    private String blacklistReason;

    private String creatorName;

    private Date createTime;

    private String deptName;

    private String userStatus;

}

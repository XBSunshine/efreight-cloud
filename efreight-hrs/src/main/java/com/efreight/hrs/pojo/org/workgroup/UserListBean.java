package com.efreight.hrs.pojo.org.workgroup;

import lombok.Data;

/**
 * @author lc
 * @date 2020/10/16 16:42
 */
@Data
public class UserListBean {

    /**
     * 数据ID
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 邮箱
     */
    private String userEmail;
    /**
     * 电话
     */
    private String phoneNumber;
    /**
     * 岗位
     */
    private String jobPosition;
    /**
     * 部门名称
     */
    private String deptName;
}

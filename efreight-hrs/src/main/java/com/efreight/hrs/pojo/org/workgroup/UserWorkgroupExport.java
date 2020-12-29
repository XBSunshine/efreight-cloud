package com.efreight.hrs.pojo.org.workgroup;

import lombok.Data;

/**
 * 用户组展示出数据Bean
 * @author lc
 * @date 2020/10/15 15:26
 */
@Data
public class UserWorkgroupExport {
    /**
     * 业务范畴
     */
    private String businessScope;
    /**
     * 工作组名
     */
    private String workgroupName;
    /**
     * 工作组备注
     */
    private String workgroupRemark;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户邮件
     */
    private String email;
    /**
     * 用户手机号
     */
    private String phone;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 职位
     */
    private String jobPosition;
}

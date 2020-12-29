package com.efreight.hrs.pojo.org.workgroup;

import lombok.Data;

/**
 * 工作组查询条件
 * @author lc
 * @date 2020/10/15 15:07
 */
@Data
public class UserWorkgroupQuery {
    /**
     * 业务域
     */
    private String businessScope;
    /**
     * 组名
     */
    private String groupName;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 企业ID
     */
    private Integer orgId;
}

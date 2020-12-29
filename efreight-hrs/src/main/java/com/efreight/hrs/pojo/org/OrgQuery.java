package com.efreight.hrs.pojo.org;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lc
 * @date 2020/5/28 14:57
 */
@Data
public class OrgQuery implements Serializable {

    /**
     * 分页信息
     */
    private Integer seize = 10;
    /**
     * 分页信息
     */
    private Integer current = 1;

    /**
     * 权限ID
     */
    private Integer permissionId;

    /**
     * 版本类型
     */
    private Integer versionType;
    /**
     * 公司名称
     */
    private String name;
    /**
     * 账户类型
     */
    private Integer accountType;
    /**
     * 数据创建日期-开始
     */
    private String cTimeStart;
    /**
     * 数据创建日期-结束
     */
    private String cTimeEnd;

}

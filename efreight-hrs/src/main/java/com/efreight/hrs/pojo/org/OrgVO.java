package com.efreight.hrs.pojo.org;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lc
 * @date 2020/5/28 14:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrgVO implements Serializable {
    //账户类型、企业编码、版本类型、创建日期、失效日期；
    /**
     * 账户类型
     */
    private String accountType;
    /**
     * 企业ID
     */
    private Integer orgId;
    /**
     * 企业编码
     */
    private String orgCode;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 版本类型
     */
    private String versionType;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 失效日期
     */
    private Date stopTime;
}

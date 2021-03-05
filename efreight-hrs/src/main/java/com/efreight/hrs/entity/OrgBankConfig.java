package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 公司银行信息
 * @author lc
 * @date 2020/12/18 16:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org_bank_config")
public class OrgBankConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "org_bank_config_id", type = IdType.AUTO)
    private Integer orgBankConfigId;

    /**
     * 企业ID
     */
    private Integer orgId;

    /**
     * 公司抬头（中文）
     */
    private String titleCn;

    /**
     * 银行账号信息（中文）
     */
    private String  bankInfoCn;

    /**
     * 地址（中文）
     */
    private String addressInfoCn;

    /**
     * 公司抬头（英文）
     */
    private String titleEn;

    /**
     * 银行账号信息（英文）
     */
    private String  bankInfoEn;

    /**
     * 地址（英文）
     */
    private String addressInfoEn;

    /**
     * 企业LOGO
     */
    private String logoUrl;

    /**
     * 企业印章
     */
    private String sealUrl;

}

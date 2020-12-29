package com.efreight.hrs.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * HRS 签约公司 服务套餐设置
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("hrs_org_bank_config")
public class OrgBankConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "org_bank_config_id", type = IdType.AUTO)
    private Integer orgBankConfigId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 公司抬头：中文
     */
    private String titleCn;

    /**
     * 公司抬头：英文
     */
    private String titleEn;

    /**
     * 银行信息：中文
     */
    private String bankInfoCn;

    /**
     * 银行信息：英文
     */
    private String bankInfoEn;

    /**
     * 地址信息：中文
     */
    private String addressInfoCn;

    /**
     * 地址信息：英文
     */
    private String addressInfoEn;

    /**
     * LOGO
     */
    private String logoUrl;

    /**
     * 印章
     */
    private String sealUrl;


}

package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 基础信息 收发货人
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_shipper_consignee")
public class ShipperConsignee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收发货人ID
     */
    @TableId(value = "sc_id", type = IdType.AUTO)
    private Integer scId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 收发货人类型：0 托运人(发货人) 1 收货人
     */
    private Integer scType;

    /**
     * 收发货人_助记码
     */
    private String scMnemonic;

    /**
     * 收发货人_名称
     */
    private String scName;

    /**
     * 收发货人_地址
     */
    private String scAddress;

    /**
     * 收发货人_代码
     */
    private String scCode;

    /**
     * 收发货人_代码类型
     */
    private String scCodeType;

    /**
     * AEO编码
     */
    private String aeoCode;

    /**
     * 国家代码
     */
    private String nationCode;

    /**
     * 国家代码中文名
     */
    @TableField(exist = false)
    private String nationNameCn;

    /**
     * 国家代码英文名
     */
    @TableField(exist = false)
    private String nationNameEn;

    /**
     * 州、省代码
     */
    private String stateCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 城市中文名称
     */
    @TableField(exist = false)
    private String cityNameCn;

    /**
     * 城市英文名称
     */
    @TableField(exist = false)
    private String cityNameEn;

    /**
     * 邮编
     */
    private String postCode;

    /**
     * 电话号码
     */
    private String telNumber;

    /**
     * 传真号码
     */
    private String faxNumber;

    /**
     * 打印备注
     */
    private String scPrintRemark;

    /**
     * 是否有效：1有效 0 无效
     */
    private Boolean isValid;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    @TableField(exist = false)
    private String columnStrs;


}

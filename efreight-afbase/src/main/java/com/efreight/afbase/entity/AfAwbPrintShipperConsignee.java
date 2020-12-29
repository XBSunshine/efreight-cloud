package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_print_shipper_consignee")
public class AfAwbPrintShipperConsignee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "awb_sc_id", type = IdType.AUTO)
    private Integer 	awbScId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 运单制单ID
     */
    private Integer awbPrintId;

    /**
     * 收发货人类型：0 发货人 1 收货人
     */
    private Integer scType;

    /**
     * 收发货人_名称
     */
    private String scName;
    /**
     * 收发货人_助记码
     */
    private String scMnemonic;
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
     * 州、省代码
     */
    private String stateCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 城市名称（作废）
     */
    private String cityName;

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

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;


}

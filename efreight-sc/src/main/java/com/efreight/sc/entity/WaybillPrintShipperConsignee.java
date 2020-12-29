package com.efreight.sc.entity;

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
 * SC 订单管理 海运制单：收发货人
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_waybill_print_shipper_consignee")
public class WaybillPrintShipperConsignee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收发货人ID
     */
    @TableId(value = "waybill_print_sc_id", type = IdType.AUTO)
    private Integer waybillPrintScId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 运单制单ID
     */
    private Integer waybillPrintId;

    /**
     * 收发货人类型：0 发货人 1 收货人
     */
    private Integer scType;

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
     * 州、省代码
     */
    private String stateCode;

    /**
     * 城市代码
     */
    private String cityCode;

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

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;


}

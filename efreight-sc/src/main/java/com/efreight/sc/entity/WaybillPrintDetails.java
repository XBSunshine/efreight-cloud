package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * CS 订单管理 海运制单-明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-08-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_waybill_print_details")
public class WaybillPrintDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运单明细ID
     */
    @TableId(value = "waybill_print_detail_id", type = IdType.AUTO)
    private Integer waybillPrintDetailId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 运单ID
     */
    private Integer waybillPrintId;

    /**
     * 明细序号
     */
    private Integer waybillPrintDetailNo;

    /**
     * 集装箱号
     */
    private String containerSealNo;

    /**
     * 数量
     */
    private String number;

    /**
     * 包装种类
     */
    private String kindOfPackage;

    @TableField(exist = false)
    private String numberAndKindOfPackage;

    /**
     * 货物名称
     */
    private String descriptionOfGoods;

    /**
     * 毛重
     */
    private String grossWeight;

    /**
     * 体积
     */
    private String volume;


}

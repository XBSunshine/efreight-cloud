package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 订单箱量详情
 * </p>
 *
 * @author lc
 * @since 2021-01-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("io_order_container_details")
public class IoOrderContainerDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 集装箱ID
     */
    @TableId(value = "container_id", type = IdType.AUTO)
    private Integer containerId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 箱型
     */
    private String containerCode;

    /**
     * 尺寸
     */
    private Integer containerSize;

    /**
     * 数量
     */
    private Integer number;

    /**
     * 标箱数量
     */
    private Integer numberTeu;
    /**
     * 箱号
     */
    private String containerNumber;
    /**
     * 封号
     */
    private String containerSealNo;

    /**
     * 件数
     */
    private Integer pieces;

    /**
     * 毛重
     */
    private BigDecimal weight;

    /**
     * 体积
     */
    private BigDecimal volume;

    /**
     * vgm重量
     */
    private BigDecimal weightVgm;


}

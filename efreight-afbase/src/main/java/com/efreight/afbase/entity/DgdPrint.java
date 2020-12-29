package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 出口订单 DGD 制单
 * </p>
 *
 * @author xiaobo
 * @since 2020-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_dgd_print")
public class DgdPrint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DGD制单ID
     */
    @TableId(value = "dgd_print_id", type = IdType.AUTO)
    private Integer dgdPrintId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 发货人Print
     */
    private String shipperPrint;

    /**
     * 收货人Print
     */
    private String consigneePrint;

    /**
     * 页码
     */
    private String pageNumber;

    /**
     * 总页数
     */
    private String pagesNumber;

    /**
     * 始发港Print
     */
    private String departureStationPrint;

    /**
     * 目的港Print
     */
    private String arrivalStationPrint;

    /**
     * handling_info
     */
    private String handlingInfo;

    /**
     * 飞机类型：0：PASSENGER AND CARGO AIRCRAFT 1： AIRCRAFT  ONLY
     */
    private Integer aircraftType;

    /**
     * 装运类型，0：NON-RADIOACTIVE  1:RADIOACTIVE
     */
    private Integer shipmentType;

    /**
     * 签字负责人
     */
    private String nameTitleOfSignatory;

    /**
     * 签字日期
     */
    private String placeAndDate;

    /**
     * 详情列表
     */
    @TableField(exist = false)
    private List<DgdPrintList> dgdPrintList;

    @TableField(exist = false)
    private String dgdPrintName;

    @TableField(exist = false)
    private String awbNumber;

    @TableField(exist = false)
    private Boolean checked = false;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;


}

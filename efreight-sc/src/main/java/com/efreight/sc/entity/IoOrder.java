package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * IO 订单管理 其他业务订单
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("io_order")
public class IoOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    /**
     * 订单uuid
     */
    private String orderUuid;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单号
     */
    private String orderCode;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 应收情况：未录收入、已录收入、已制账单、部分核销、核销完毕
     */
    private String incomeStatus;

    /**
     * 应付情况：未录成本、已录成本、已对账、部分核销、核销完毕
     */
    private String costStatus;

    /**
     * 收入完成：1是  0否
     */
    private Boolean incomeRecorded;

    /**
     * 成本完成：1是  0否
     */
    private Boolean costRecorded;

    @TableField(exist = false)
    private Integer incomeRecordedForSort;
    @TableField(exist = false)
    private Integer costRecordedForSort;

    /**
     * 客户单号
     */
    private String customerNumber;

    /**
     * 关联订单号
     */
    private String orderCodeAssociated;

    /**
     * 客户ID
     */
    private Integer coopId;

    @TableField(exist = false)
    private String coopName;

    /**
     * 业务范畴 OB
     */
    private String businessScope;

    /**
     * 业务分类
     */
    private String businessMethod;

    /**
     * 业务日期
     */
    private LocalDate businessDate;
    @TableField(exist = false)
    private LocalDate businessDateStart;
    @TableField(exist = false)
    private LocalDate businessDateEnd;
    /**
     * 始发地
     */
    private String departureStation;

    /**
     * 目的地
     */
    private String arrivalStation;

    /**
     * 货物品名_英文
     */
    private String goodsNameEn;

    /**
     * 货物品名_中文
     */
    private String goodsNameCn;

    /**
     * 货物类型
     */
    private String goodsType;

    /**
     * 包装类型
     */
    private String packageType;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 预报件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer planPieces;
    @TableField(exist = false)
    private String planPiecesStr;

    /**
     * 预报毛重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planWeight;

    @TableField(exist = false)
    private String planWeightStr;
    /**
     * 预报体积
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planVolume;
    @TableField(exist = false)
    private String planVolumeStr;

    /**
     * 预报计重
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private BigDecimal planChargeWeight;
    @TableField(exist = false)
    private String planChargeWeightStr;

    /**
     * 责任客服ID
     */
    private Integer servicerId;

    /**
     * 责任客服名称
     */
    private String servicerName;

    /**
     * 责任销售ID
     */
    private Integer salesId;

    /**
     * 责任销售名称
     */
    private String salesName;

    /**
     * 创建人
     */
    private Integer creatorId;

    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    @TableField(exist = false)
    private LocalDateTime createTimeStart;
    @TableField(exist = false)
    private LocalDateTime createTimeEnd;


    /**
     * 修改人
     */
    private Integer editorId;

    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;

    /**
     * rowid
     */
    private String rowUuid;

    @TableField(exist = false)
    private String columnStrs;

    @TableField(exist = false)
    private IoOrderShipperConsignee shipper;
    @TableField(exist = false)
    private IoOrderShipperConsignee consignee;

    /**
     * 工作组ID
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer workgroupId;

    /**
     * 当前用户的订单权限
     */
    @TableField(exist = false)
    private Integer orderPermission;

    /**
     * 当前用户ID
     */
    @TableField(exist = false)
    private Integer currentUserId;

    /**
     * 集装箱量
     */
    private String containerList;

    /**
     * 集装箱量明细
     */
    @TableField(exist = false)
    private List<IoOrderContainerDetails> containerDetails;

    @TableField(exist = false)
    private String containerNumberAndContainerSealNo;

}

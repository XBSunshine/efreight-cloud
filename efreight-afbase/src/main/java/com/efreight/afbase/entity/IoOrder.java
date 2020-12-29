package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * IO 订单管理 其他业务订单
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
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

    private String packageType;

    /**
     * 货物类型
     */
    private String goodsType;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 预报件数
     */
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    /**
     * 预报体积
     */
    private BigDecimal planVolume;

    /**
     * 预报计重
     */
    private BigDecimal planChargeWeight;

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


}

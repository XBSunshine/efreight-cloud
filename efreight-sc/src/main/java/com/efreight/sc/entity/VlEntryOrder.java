package com.efreight.sc.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author qipm
 * @since 2021-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("vl_entry_order")
public class VlEntryOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "entry_order_id", type = IdType.AUTO)
    private Integer entryOrderId;
    /**
     * 订单uuid
     */
    private String entryOrderUuid;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 订单号
     */
    private String entryOrderCode;

    /**
     * 入区登记申报时间
     */
    private LocalDateTime entryDeclareDatetime;

    /**
     * 入区登记申报状态
     */
    private String entryDeclareStatus;

    /**
     * 入区申报人id
     */
    private Integer entryDeclareId;

    /**
     * 入区申报人名称
     */
    private String entryDeclareName;

    /**
     * 入区预约申报时间
     */
    private LocalDateTime appointDeclareDatetime;

    /**
     * 入区预约申报状态
     */
    private String appointDeclareStatus;

    /**
     * 入区预约人id
     */
    private Integer appointDeclareId;

    /**
     * 入区预约人名称
     */
    private String appointDeclareName;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    /**
     * 提货监管仓库代码
     */
    private String warehouseCode;

    /**
     * 主管海关代码
     */
    private String masterCustom;

    /**
     * 申报消息的ID号
     */
    private String mft8802024MessageId;

    /**
     * 自助进出区登记编号
     */
    private String icSeq;

    /**
     * 车牌号
     */
    private String vehicleNo;

    /**
     * 车队标识
     */
    private Integer vehteamFlag;

    /**
     * 车队车牌号
     */
    private String vehteamNo;

    /**
     * 司机驾驶证号
     */
    private String driverInfo;

    /**
     * 转关标识
     */
    private Integer transInFlag;

    /**
     * 预约入园时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime appointmentDatetime;

    /**
     * 交运类型
     */
    private String transportType;

    /**
     * 进区托架编号
     */
    private String trailerNoIn;

    /**
     * 进区托架重量
     */
    private BigDecimal trailerWeightIn;

    /**
     * 出区托架编号
     */
    private String trailerNoOut;

    /**
     * 出区托架重量
     */
    private BigDecimal trailerWeightOut;

    /**
     * 进区集装箱空重
     */
    private BigDecimal contWeightIn;

    /**
     * 出区集装箱空重
     */
    private BigDecimal contWeightOut;

    /**
     * 备注
     */
    private String note;

    private transient Integer vlOrderId;
    private transient String mawbNumbers;
    private transient String symbol;
    private transient String text;
    private transient String seq;
    private transient String declareDate;

    @TableField(exist = false)
    private List<VlEntryOrderDetail> vlEntryOrderDetails;
}

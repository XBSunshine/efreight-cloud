package com.efreight.afbase.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 订单管理 出口订单 托书信息
 * </p>
 *
 * @author qipm
 * @since 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_shipper_letter")
public class AfShipperLetter implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "sl_id", type = IdType.AUTO)
    private Integer slId;
    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 托书类型：主单MAWB，分单HAWB
     */
    private String slType;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 订单UUID
     */
    private String orderUuid;

    /**
     * 分单号
     */
    private String hawbNumber;
    //目的港
    private String arrivalStation;

    /**
     * 中转港
     */
    private String transitStation;
    /**
     * 中文品名
     */
    private String goodsNameCn;

    /**
     * 托书品名(英文品名)
     */
    private String goodsNameEn;

    /**
     * 托书件数
     */
    private Integer planPieces;

    /**
     * 托书毛重
     */
    private BigDecimal planWeight;

    /**
     * 体积
     */
    private BigDecimal planVolume;

    /**
     * 尺寸
     */
    private String planDimensions;

    /**
     * 海关代码
     */
    private String customsStatusCode;

    /**
     * 随机文件
     */
    private String airborneDocument;

    /**
     * 鉴定情况
     */
    private String appraisalNote;

    /**
     * 发货人
     */
    private String consignorName;

    /**
     * 收货人
     */
    private String consigneeName;

    /**
     * handling_info
     */
    private String handlingInfo;

    /**
     * 唛头
     */
    private String shippingMarks;

    private String paymentMethod;

    private LocalDateTime loadingDate;

    @TableField("UNDG_code")
    private String undgCode;
    @TableField("UNDG_contact_name")
    private String undgContactName;
    @TableField("UNDG_contact_communication_type")
    private String undgContactCommunicationType;
    @TableField("UNDG_contact_communication_no")
    private String undgContactCommunicationNo;

    private Boolean partialShipment;
    /**
     * 托书操作备注
     */
    private String slRemark;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private String editorName;

    private LocalDateTime editTime;

    /**
     * 舱单状态
     */
    private String shippingBillState;

    /**
     * 申报状态
     */
    private String declareState;

    private transient String mawbNumber;
    private transient String orderCode;
    //始发港
    private String departureStation;
    private Integer awbId;
    private Integer consignorId;
    private Integer consigneeId;

    @TableField(exist = false)
    private Boolean hawbChecked;

    //收发货人
    @TableField(exist = false)
    private AfOrderShipperConsignee afOrderShipperConsignee1;
    @TableField(exist = false)
    private AfOrderShipperConsignee afOrderShipperConsignee2;

    private String expectFlight;
    private LocalDate expectDeparture;
    @TableField(exist = false)
    private String awbNumber;

    private String unloadingLocationCode;
    private LocalDate unloadingLocationDate;
    private LocalDateTime actualDatetime;
    private LocalDateTime completedDatetime;
}

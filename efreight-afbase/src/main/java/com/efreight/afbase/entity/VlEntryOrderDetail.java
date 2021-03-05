package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
@TableName("vl_entry_order_detail")
public class VlEntryOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "entry_order_detail_id", type = IdType.AUTO)
    private Integer entryOrderDetailId;
    /**
     * 入区登记订单ID
     */
    private Integer entryOrderId;

    /**
     * 业务订单ID
     */
    private Integer orderId;

    /**
     * 入区登记序号
     */
    private Integer seqNo;

    /**
     * 总提运单号
     */
    private String mawbNumber;

    /**
     * 分提运单号
     */
    private String hawbNumber;

    /**
     * 报关单号
     */
    private String docNo;

    /**
     * 货物分类
     */
    private String classType;

    /**
     * 货物性质
     */
    private String cargoType;

    /**
     * 航班号
     */
    private String flightNo;

    /**
     * 航班日期
     */
    private LocalDate flightDate;

    /**
     * 目的地
     */
    private String destination;

    /**
     * 中文品名
     */
    private String goodsName;

    /**
     * 英文品名
     */
    private String goodsEname;

    /**
     * 托运货物件数
     */
    private Integer pieces;

    /**
     * 货物总毛重
     */
    private BigDecimal totalWeight;

    /**
     * 预报体积
     */
    private BigDecimal predictionVolume;

    /**
     * 搬运公司ID
     */
    private Integer handlingCompany;

    /**
     * 搬运公司名称
     */
    private String handlingCompanyName;

    /**
     * 包裹大小
     */
    private String packageSize;

    /**
     * 打板公司
     */
    private String battleName;

    /**
     * 入仓公司
     */
    private String wareName;

    /**
     * 航空公司
     */
    private String airlineName;

    /**
     * 备注
     */
    private String note;

    private String warehouseCode;
}

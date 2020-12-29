package com.efreight.sc.entity.view;

import com.efreight.sc.entity.Order;
import com.efreight.sc.entity.OrderFiles;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单轨迹
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderTrackVO implements Serializable {
    private Integer orderId;
    private String orderUUID;
    /**
     * 主提单号
     */
    private String mlbNumber;
    /**
     * 分提单号
     */
    private String hlbNumber;
    /**
     * 订单号
     */
    private String orderCode;

    /**
     * 始发港
     */
    private String departureStation;
    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 船名
     */
    private String shipName;
    /**
     * 船次号
     */
    private String shipVoyageNumber;
    /**
     * 离港日期
     */
    private String expectDeparture;
    /**
     * 合约号
     */
    private String customerNumber;
    /**
     * 中文品名
     */
    private String goodsNameCn;
    /**
     * 货物类型
     */
    private String goodsType;
    /**
     * 装箱方式
     */
    private String containerMethod;
    /**
     *集装箱量
     */
    private String containerList;
    /**
     * 标箱数量
     */
    private Integer containerNumber;
    //包装类型
    private String packageType;
    //预报件数
    private Integer planPieces;
    //预计毛重
    private BigDecimal planWeight;
    //预报体积
    private BigDecimal planVolume;
    //预报计费重
    private BigDecimal planChargeWeight;
    /**
     * 入库日期
     */
    private String inboundDate;
    /**
     * 出库日期
     */
    private String outboundDate;

    /**
     *检查日期
     */
    private String inspectionDate;
    /**
     * 放行日期
     */
    private String clearanceDate;
    //附件信息
    List<OrderFiles> attachments;
    //轨迹信息
    List routeTracks;

    public void addOrder(Order order) {
        this.orderId = order.getOrderId();
        this.orderUUID = order.getOrderUuid();
        this.mlbNumber = order.getMblNumber();
        this.hlbNumber = order.getHblNumber();
        this.orderCode = order.getOrderCode();
        this.departureStation = order.getDepartureStation();
        this.arrivalStation = order.getArrivalStation();
        this.shipName = order.getShipName();
        this.shipVoyageNumber = order.getShipVoyageNumber();
        this.expectDeparture = formatDateToString(order.getExpectDeparture(), "yyyy-MM-dd");
        this.customerNumber = order.getCustomerNumber();
        this.goodsNameCn = order.getGoodsNameCn();
        this.goodsType = order.getGoodsType();
        this.containerMethod = order.getContainerMethod();
        this.containerList = order.getContainerList();
        this.containerNumber = order.getContainerNumber();
        this.packageType = order.getPackageType();
        this.planPieces = order.getPlanPieces();
        this.planWeight = order.getPlanWeight();
        this.planVolume = order.getPlanVolume();
        this.planChargeWeight = order.getPlanChargeWeight();

        this.inboundDate = formatDateToString(order.getInboundDate(), "yyyy-MM-dd");
        this.outboundDate = formatDateToString(order.getOutboundDate(), "yyyy-MM-dd");
        this.inspectionDate = formatDateToString(order.getCustomsInspectionDate(), "yyyy-MM-dd");
        this.clearanceDate = formatDateToString(order.getCustomsClearanceDate(), "yyyy-MM-dd");
    }

    private String formatDateToString(LocalDate localDate, String pattern){
        if(null != localDate){
            return localDate.format(DateTimeFormatter.ofPattern(pattern));
        }
        return null;
    }
}

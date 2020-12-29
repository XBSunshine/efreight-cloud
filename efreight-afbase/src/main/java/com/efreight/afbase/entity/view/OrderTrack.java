package com.efreight.afbase.entity.view;

import com.efreight.afbase.entity.AfOrder;
import com.efreight.afbase.entity.OrderFiles;
import com.efreight.afbase.entity.route.AfAwbRouteTrackAwb;
import com.efreight.afbase.entity.route.AfAwbRouteTrackManifest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * 订单轨迹
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer orderId;
    private String orderUUID;
    //主单号
    private String awbNumber;
    //订单号
    private String orderCode;
    //业务单号
    private String customerNumber;
    //预计航班号
    private String expectFlight;
    //预计航班日期
    private String expectDeparture;
    //始发港
    private String departureStation;
    //目的港
    private String arrivalStation;
    //中文品名
    private String goodsNameCn;
    //英文品名
    private String goodsNameEn;
    //预报件数
    private Integer planPieces;
    //实际出重件数
    private Integer confirmPieces;
    //包装类型
    private String packageType;
    //预计毛重
    private BigDecimal planWeight;
    //实际出重毛重
    private BigDecimal confirmWeight;
    //毛重单位
    private String weightUnit;
    //预报体积
    private Double planVolume;
    //实际出重体积
    private Double confirmVolume;
    //体积单位
    private String volumeUnit;
    //预报计费重
    private Double planChargeWeight;
    //实际出重
    private Double confirmChargeWeight;
    //计费重量单位
    private String chargeWeightUnit;
    //附件信息
    List<OrderFiles> attachments;
    //轨迹信息
    List<AfAwbRouteTrackAwb> routeTracks;
    /**
     * 分单号
     */
    private String hawbNumber;

    /**
     * 货物类型
     */
    private String goodsType;
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
    /**
     * 舱单信息
     */
    List<AfAwbRouteTrackManifest> trackManifest;

    /**
     * 舱单信息（页展示)
     */
    LinkedList<ManifestVO> manifestList;


    public void addOrder(AfOrder afOrder) {
        if(null == afOrder){
            return;
        }
        this.orderId = afOrder.getOrderId();
        this.orderUUID = afOrder.getOrderUuid();
        this.awbNumber = afOrder.getAwbNumber();
        this.orderCode = afOrder.getOrderCode();
        this.customerNumber = afOrder.getCustomerNumber();
        this.expectFlight = afOrder.getExpectFlight();
        LocalDate localDate = afOrder.getExpectDeparture();
        if(null != localDate){
            this.expectDeparture = localDate.format(DateTimeFormatter.ofPattern("dd MMM"));
        }
        this.departureStation = afOrder.getDepartureStation();
        this.arrivalStation = afOrder.getArrivalStation();

        this.goodsNameCn = afOrder.getGoodsNameCn();
        this.goodsNameEn = afOrder.getGoodsNameEn();

        this.planPieces = afOrder.getPlanPieces();
        this.confirmPieces = afOrder.getConfirmPieces();
        this.packageType = afOrder.getPackageType();

        this.planWeight = afOrder.getPlanWeight();
        this.confirmWeight = afOrder.getConfirmWeight();

        this.planVolume = afOrder.getPlanVolume();
        this.confirmVolume = afOrder.getConfirmVolume();

        this.planChargeWeight = afOrder.getPlanChargeWeight();
        this.confirmChargeWeight = afOrder.getConfirmChargeWeight();

        this.hawbNumber = afOrder.getHawbNumber();

        this.goodsType = afOrder.getGoodsType();

        this.inboundDate = formatDateToString(afOrder.getInboundDate(), "yyyy-MM-dd");
        this.outboundDate = formatDateToString(afOrder.getOutboundDate(), "yyyy-MM-dd");

        this.inspectionDate = formatDateToString(afOrder.getCustomsInspectionDate(), "yyyy-MM-dd");
        this.clearanceDate = formatDateToString(afOrder.getCustomsClearanceDate(), "yyyy-MM-dd");

    }

    private String formatDateToString(LocalDate localDate, String pattern){
        if(null != localDate){
            return localDate.format(DateTimeFormatter.ofPattern(pattern));
        }
        return null;
    }
}

package com.efreight.ws.afbase.pojo.order.edit;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class EditOrderRequest implements Serializable {
    @NotNull(message = "参数错误：订单号")
    private String orderCode;
    private String coopCode;
    private String storehouseCode;
    private String departureStation;
    private String arrivalStation;
    private String expectDeparture;
    private Integer planPieces;
    private BigDecimal planWeight;
    private Double planVolume;
    private Double freightUnitPrice;
    private String consigneePrintRemark;
    private String consignorPrintRemark;
    private String serviceName;;
    private String salesName;
    private String awbNumber;
    private String operationRemark;
    private String freightProfitRatioRemark;
    private String orderRemark;
    private String priceRemark;

}

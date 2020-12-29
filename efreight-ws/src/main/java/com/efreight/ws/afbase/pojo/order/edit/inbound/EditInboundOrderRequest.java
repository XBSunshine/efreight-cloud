package com.efreight.ws.afbase.pojo.order.edit.inbound;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class EditInboundOrderRequest implements Serializable {
    private String orderCode;
    private String awbNumber;
    private Integer orderPieces;
    private BigDecimal orderGrossWeight;
    private Double orderVolume;
    private String orderVolumeWeight;
    private Double orderChargeWeight;
    private String receiptDate;
    private String warehouseCode;
}

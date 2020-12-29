package com.efreight.ws.afbase.pojo.order.detail;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ShipperLetter implements Serializable {
    private String hawbNumber;
    private String arrivalStation;
    private String goodsNameCn;
    private Integer planPieces;
    private BigDecimal planWeight;
}

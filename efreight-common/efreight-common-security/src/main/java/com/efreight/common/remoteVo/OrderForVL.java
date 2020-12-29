package com.efreight.common.remoteVo;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderForVL {

    private Integer orderId;

    private String orderUuid;

    private String orderCode;

    private String businessScope;

    private String awbNumber;

    private String customerNumber;

    private LocalDate flightDate;
    private LocalDate flightDateStart;
    private LocalDate flightDateEnd;

    private Integer planPieces;
    private Integer confirmPieces;

    private BigDecimal planVolume;
    private BigDecimal confirmVolume;

    private BigDecimal planWeight;
    private BigDecimal confirmWeight;

    private BigDecimal planChargeWeight;
    private BigDecimal confirmChargeWeight;

    private String noOrderIds;

}

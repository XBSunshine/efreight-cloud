package com.efreight.afbase.entity.procedure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AirCargoManifestPrint {

    //共享信息
    private String flag;
    private String departureStation;
    private String arrivalStation;
    private Integer pieces;
    private BigDecimal weight;
    private String shipperName;
    private String consigneeName;
    private String goodsName;

    //主单信息
    private String awbNumber;
    private String flightNo;
    private LocalDate flightDate;

    //头部信息
    private String pageInfo;
    //列表信息
    private List<AirCargoManifestPrint> list;
}

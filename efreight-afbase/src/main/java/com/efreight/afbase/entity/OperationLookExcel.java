package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OperationLookExcel implements Serializable {

    /**
     * 主单号
     */
    private String awbNumber;
    /**
     * 分单号
     */
    private String hawbNumber;
    private String coopName;
    private String departureStation;
    private String arrivalStation;
    private String expectFlight;
    private LocalDate expectDeparture;
    private String departureWarehouseName;
    private transient String presets;
    private transient String arrived;
    private transient String passed;
    private transient String ams;
    private transient String entryPlate;



    /**
     * 预报件数
     */
    private Integer planPieces;

    /**
     * 预报毛重
     */
    private BigDecimal planWeight;

    /**
     * 预报体积
     */
    private Double planVolume;
    /**
     * 实际件数
     */
    private Integer confirmPieces;

    /**
     * 实际毛重
     */
    private BigDecimal confirmWeight;

    /**
     * 实际体积
     */
    private Double confirmVolume;

}

package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ShipperConsigneeExcel {
    /**
     * 类型
     */
    private String scType;
    /**
     * 代码
     */
    private String scCode;

    /**
     * 名称
     */
    private String scName;

    /**
     * 国家
     */
    private String nationNameEn;

    /**
     * 城市
     */
    private String cityNameEn;

    /**
     * 是否生效
     */
    private String isValid;

}

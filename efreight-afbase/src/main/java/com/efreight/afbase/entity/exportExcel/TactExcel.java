package com.efreight.afbase.entity.exportExcel;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TactExcel {
    private String dataSource;

    /**
     * 航司代码
     */
    private String carrierCode;
    /**
     * 始发港
     */
    private String departureStation;

    /**
     * 目的港
     */
    private String arrivalStation;
    /**
     * 开始时间
     */
    private String beginDate;

    /**
     * 结束时间
     */
    private String endDate;

    private String tactM;
    private String tactN;
    @TableField("tact_45")
    private String tact45;
    @TableField("tact_100")
    private String tact100;
    @TableField("tact_300")
    private String tact300;
    @TableField("tact_500")
    private String tact500;
    @TableField("tact_700")
    private String tact700;
    @TableField("tact_1000")
    private String tact1000;
    @TableField("tact_2000")
    private String tact2000;
    @TableField("tact_3000")
    private String tact3000;
    @TableField("tact_5000")
    private String tact5000;

    private String tactRemark;

}

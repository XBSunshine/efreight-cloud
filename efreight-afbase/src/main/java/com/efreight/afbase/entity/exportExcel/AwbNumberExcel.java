package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AwbNumberExcel {

    private String awbNumber;
    private String departureStation;
    private String awbFromTypeAndName;
    private String awbStatus;
    private String creatorName;
    private Date creatTime;
}

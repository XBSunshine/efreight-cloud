package com.efreight.afbase.entity.procedure;

import com.efreight.afbase.entity.AwbPrint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AfPAwbPrintProcedure {

    private Integer orgId;

    private String awbPrintType;

    private String awbUuid;

    private String orderUuid;

    private String hawbNumber;

    private Integer printType;

    private Integer awbPrintId;

    private AwbPrint awbPrint;

    private Integer slId;

}

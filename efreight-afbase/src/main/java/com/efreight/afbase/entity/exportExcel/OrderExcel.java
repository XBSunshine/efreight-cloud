package com.efreight.afbase.entity.exportExcel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OrderExcel {

    /**
     * 主单号
     */
    private String awbNumber;
    private String coopName;
    private String goodsNameCn;
    private String createTime;
    //
    private Integer storagePieces;
    private BigDecimal storageWeight;
    private String storageTime;
}

package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDeliveryNoticeCheck implements Serializable {
    /**
     * 是否通过：
     *  true 通过
     *  false 未通过
     */
    private boolean passed;
    /**
     * 仓库/货栈名
     */
    private String warehouseName;

    public OrderDeliveryNoticeCheck(boolean passed, String warehouseName){
        this.passed = passed;
        this.warehouseName = warehouseName;
    }
}

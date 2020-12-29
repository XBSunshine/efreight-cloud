package com.efreight.ws.afbase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_warehouse")
public class WSWarehouse implements Serializable {
    @TableId(value = "warehouse_id", type = IdType.AUTO)
    private Integer warehouseId;
    private Integer orgId;
    private String businessScope;
    private String warehouseCode;
    private String apCode;
    private String warehouseNameCn;
    private String warehouseNameEn;
    private String customsSupervision;
    private Integer warehouseStatus;
}

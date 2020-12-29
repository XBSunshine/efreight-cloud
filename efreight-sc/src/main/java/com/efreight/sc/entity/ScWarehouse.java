package com.efreight.sc.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_warehouse")
public class ScWarehouse implements Serializable{
	
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@TableId(value = "warehouse_id", type = IdType.AUTO)
	  private Integer warehouseId;
	  
	  private Integer orgId;
	  
	  private String businessScope;
	  
	  private String warehouseCode;
	  
	  private String apCode;
	  
	  private String warehouseNameCn;
	  
	  private String warehouseNameEn;
	  
	  private String warehouseLongitude;
	  
	  private String warehouseLatitude;
	  
	  private String warehouseAddressGps;
	  
	  private Integer warehouseStatus;
	  
	  private String customsSupervision;
	  
	  private String customsCode;
	  
	  private String agentCoopId;
	  
	  private String deliverTemplate;
	  
	  private Integer shipperTemplate;
	  
	  private String labelTempalteAwb;
	  
	  private String labelTempalteHwb;
	  
	  private String warehouseContactRemark;
	  
	  private String delivererContactRemark;
	  
	  private Integer creatorId;
	  
	  private String creatorName;
	  
	  private LocalDateTime createTime;
	  
	  private Integer editorId;
	  
	  private String editorName;
	  
	  private LocalDateTime editTime;
	  
	  @TableField(exist = false)
	  private String operateName;
	  @TableField(exist = false)
	  private LocalDateTime operateTime;
	  @TableField(exist = false)
	  private String portNameEn;
	  @TableField(exist = false)
	  private String warehouseCodeCheck;
	  @TableField(exist = false)
	  private String warehouseNameCnCheck;

}

package com.efreight.afbase.entity;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
@TableName("af_order_share")
public class AfOrderShare implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@TableId(value = "order_share_id", type = IdType.AUTO)
	private Integer orderShareId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer orgId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer orderId;
	private String businessScope;
	private String shareScope;
	private String process;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer shareCoopId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer shareOrgId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer shareOrderId;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer creatorId;
	private String creatorName;
	private LocalDateTime createTime;
	@TableField(strategy = FieldStrategy.IGNORED)
	private Integer editorId;
	private String editorName;
	private LocalDateTime editTime;
	@TableField(exist = false)
	private List<Integer> listOrderFilesId;
	@TableField(exist = false)
	private String orderUuid;
	@TableField(exist = false)
	private Integer orderShareCoopId;
	@TableField(exist = false)
	private Integer orderShareOrgId;
	@TableField(exist = false)
	private Integer orderShareOrderId;
	@TableField(exist = false)
	private String coopName;
	

}

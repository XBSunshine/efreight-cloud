package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author qipm
 * @since 2020-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_cargo_goodsnames")
public class CargoGoodsnames implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 中文品名
     */
    private String goodsCnnames;

    /**
     * 英文品名
     */
    private String goodsEnnames;

    /**
     * 货物类型
     */
    private String cargoType;

    /**
     * 件数
     */
    @TableField(strategy = FieldStrategy.IGNORED)
    private Integer quantity;

    /**
     * 鉴定编号
     */
    private String reportIssueNo;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    private String orgCode;

    private String orgName;

    private Integer creatorId;

    private String creatorName;

    private LocalDateTime createTime;

    private Integer editorId;

    private LocalDateTime editTime;

    private String editorName;


}

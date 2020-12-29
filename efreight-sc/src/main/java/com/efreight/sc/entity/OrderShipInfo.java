package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_order_ship_info")
public class OrderShipInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 船公司信息ID
     */
    @TableId(value = "ship_info_id", type = IdType.AUTO)
    private Integer shipInfoId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 船名中文
     */
    private String shipNameCn;

    /**
     * 船名英文
     */
    private String shipNameEn;

    /**
     * 是否启用
     */
    private Boolean isValid;

    private Integer creatorId;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private Integer editorId;

    /**
     * 修改人
     */
    private String editorName;

    /**
     * 修改时间
     */
    private LocalDateTime editTime;
    /**
     * 前段查询模糊名称
     */
    @TableField(exist = false)
    private String shipName;
    @TableField(exist = false)
    private String isValidStr;


}

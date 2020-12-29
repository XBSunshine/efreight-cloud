package com.efreight.sc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 船司表
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sc_ship_company")
public class ShipCompany implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "ship_company_id", type = IdType.AUTO)
    private Integer shipCompanyId;

    /**
     * 船司代码
     */
    private String shipCompanyCode;

    /**
     * 中文名称
     */
    private String shipCompanyNameCn;

    /**
     * 英文名称
     */
    private String shipCompanyNameEn;

    /**
     * 是否启用
     */
    private Boolean isValid;

    /**
     * 创建人ID
     */
    private Integer creatorId;
    /**
     * 创建人
     */
    private String creatorName;
    /**
     * 创建日期
     */
    private Date createTime;
    /**
     * 修改人ID
     */
    private Integer editorId;
    /**
     * 修改人
     */
    private String editorName;
    /**
     * 修改日期
     */
    private Date editTime;


}

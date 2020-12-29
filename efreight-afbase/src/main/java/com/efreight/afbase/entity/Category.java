package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 参数表
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 参数分类
     */
    private Integer categoryType;

    /**
     * 参数分类名称
     */
    private String categoryName;

    /**
     * 参数排序
     */
    private Integer paramRanking;

    /**
     * 参数名称
     */
    private String paramText;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 备注
     */
    private String remarks;

    /**
     * EDI编码1
     */
    @TableField("EDICode1")
    private String EDICode1;

    /**
     * EDI编码2
     */
    @TableField("EDICode2")
    private String EDICode2;


}

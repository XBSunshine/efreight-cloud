package com.efreight.sc.entity;

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
 * IO 基础信息 参数列表
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("io_category")
public class IoCategory implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 参数有效性
     */
    private Boolean isValid;

    /**
     * 参数备注
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

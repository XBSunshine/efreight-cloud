package com.efreight.afbase.entity.view;

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
 * VIEW
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_af_category")
public class VAfCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 参数分类名称
     */
    private String categoryName;

    /**
     * 参数名称
     */
    private String paramText;

    /**
     * 参数排序
     */
    private Integer paramRanking;

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

    /**
     * 参数备注
     */
    private String remarks;


}

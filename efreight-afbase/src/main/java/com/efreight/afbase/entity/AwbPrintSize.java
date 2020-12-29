package com.efreight.afbase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 操作管理 运单制单 尺寸表
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_awb_print_size")
public class AwbPrintSize implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运单尺寸ID
     */
    @TableId(value = "awb_size_id", type = IdType.AUTO)
    private Integer awbSizeId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 运单制单ID
     */
    private Integer awbPrintId;

    /**
     * 件数
     */
    private Integer awbSizePieces;

    /**
     * 长
     */
    private BigDecimal awbSizeLength;

    /**
     * 宽
     */
    private BigDecimal awbSizeWidth;

    /**
     * 高
     */
    private BigDecimal awbSizeHeight;


}

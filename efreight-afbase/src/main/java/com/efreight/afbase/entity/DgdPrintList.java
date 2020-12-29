package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 出口订单 DGD 制单 明细
 * </p>
 *
 * @author xiaobo
 * @since 2020-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_dgd_print_list")
public class DgdPrintList implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DGD制单明细ID
     */
    @TableId(value = "dgd_print_list_id", type = IdType.AUTO)
    private Integer dgdPrintListId;

    /**
     * DGD制单ID
     */
    private Integer dgdPrintId;

    /**
     * 签约公司ID
     */
    private Integer orgId;

    /**
     * 序号
     */
    private Integer no;

    /**
     * UN or ID NO
     */
    private String unIdNo;

    /**
     * proper shipping name
     */
    private String properShippingName;

    /**
     * class or division
     */
    private String classOrDivision;

    /**
     * packing group
     */
    private String packingGroup;

    /**
     * Quantity and type of packing
     */
    private String quantityAndTypeOfPacking;

    /**
     * Packing inst
     */
    private String packingInst;

    /**
     * Authorization
     */
    private String authorization;


}

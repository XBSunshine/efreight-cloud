package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_prm_category")
public class VPrmCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer categoryType;
    private String categoryName;
    private Integer paramRanking;
    private String paramText;
    private String remarks;
    @TableField("EDICode1")
    private String edicode1;
}

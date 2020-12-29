package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_V_prm_coop")
public class VPrmCoop implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer orgId;
    private Integer coopId;
    private String coopType;
    private String coopCode;
    private String coopMnemonic;
    private String coopName;
    private String shortName;
    private String businessScopeAE;
    private String businessScopeAI;
    private String businessScope;
}

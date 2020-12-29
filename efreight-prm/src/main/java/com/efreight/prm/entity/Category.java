package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Category implements Serializable {
    private Integer categoryId;
    private Integer categoryType;
    private String categoryName;
    private Integer paramId;
    private String paramText;
    private Integer isVolid;
    private String remark;
    private String eDICode1;
    private String eDICode2;
}

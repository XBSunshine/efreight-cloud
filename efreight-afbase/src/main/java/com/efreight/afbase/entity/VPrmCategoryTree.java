package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VPrmCategoryTree {
    private String serviceId;
    private String serviceNameCn;
    private String serviceRemark;
    private Boolean parent = true;
    private List<Service> services;
}

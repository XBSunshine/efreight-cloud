package com.efreight.prm.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CategoryTree {

    private String categoryId;

    /**
     * 参数分类名称
     */
    private String paramText;

    /**
     * 参数分类
     */
    private Integer categoryType;

    /**
     * 参数列表
     */
    private List<Category> params;
}

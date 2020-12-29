package com.efreight.prm.service;

import com.efreight.prm.entity.Category;
import com.efreight.prm.entity.CategoryTree;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    Map<String,Object> findParamListCriteria(Category category, Integer currentPage, Integer pageSize);
    void deleteParam(Category category);
    List<Map> findCategoryList();
    void createParam(Category category);
    Category findParamCriteria(Category category);
    void modifyParam(Category category);

    List<CategoryTree> findParamTree(Category category);

    List<Category> getParamsNew(String categoryName);

    List<Category> getSettlementPeriods(String categoryName);
}

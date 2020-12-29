package com.efreight.prm.dao;

import com.efreight.prm.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CategoryDao {
    List<Category> findParamListCriteria(Category category);

    void deleteParam(Category category);

    List<Map> findCategoryList();

    void createParam(Category category);

    Category findParamCriteria(Category category);

    void modifyParam(Category category);

    List<Category> findParamsByCategoryType(@Param("categoryType") String categoryType, @Param("paramText") String paramText, @Param("isVolid") Integer isVolid);

    List<Map> findCategoryListCriteria(@Param("categoryName") String categoryName);

    List<Category> getParamsNew(@Param("categoryName") String categoryName);

    List<Category> getSettlementPeriods(@Param("categoryName") String categoryName);
}

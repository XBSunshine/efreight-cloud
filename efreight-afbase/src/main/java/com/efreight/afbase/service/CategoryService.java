package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfVPrmCategory;
import com.efreight.afbase.entity.Category;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.CategoryTree;

import java.util.List;

/**
 * <p>
 * 参数表 服务类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CategoryService extends IService<Category> {

	IPage<Category> getPage(Page page, Category category);

    List<CategoryTree> getTree(Category category);

    List<Category> getParams(String categoryName);

    List<Category> findCategory(String categoryName);

    List<AfVPrmCategory> getAfVPrmCategory(String categoryName);

    List<AfVPrmCategory> queryCategoryByCategoryType(Integer categoryType);
}

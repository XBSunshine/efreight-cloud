package com.efreight.prm.service.impl;

import com.efreight.prm.dao.CategoryDao;
import com.efreight.prm.entity.Category;
import com.efreight.prm.entity.CategoryTree;
import com.efreight.prm.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    /**
     * 有条件查询列表数据
     *
     * @param category
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> findParamListCriteria(Category category, Integer currentPage, Integer pageSize) {
        Page<Category> page = PageHelper.startPage(currentPage, pageSize);
        List<Category> paramList = categoryDao.findParamListCriteria(category);
        Integer totalNum = Integer.parseInt(page.getTotal() + "");
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("paramList", paramList);
        return resultMap;
    }

    /**
     * 删除
     *
     * @param category
     */
    @Override
    public void deleteParam(Category category) {
        categoryDao.deleteParam(category);
    }

    /**
     * 查询所有参数类型
     *
     * @return
     */

    @Override
    public List<Map> findCategoryList() {
        return categoryDao.findCategoryList();
    }

    /**
     * 新建参数
     *
     * @param category
     */
    @Override
    public void createParam(Category category) {
        //非空判断
        if (StringUtils.isEmpty(category.getCategoryId())) {
            throw new RuntimeException("参数类型序号不能为空");
        }
        if (StringUtils.isEmpty(category.getCategoryName())) {
            throw new RuntimeException("参数类型名称不能为空");
        }
        if (StringUtils.isEmpty(category.getParamId())) {
            throw new RuntimeException("参数序号不能为空");
        }
        if (StringUtils.isEmpty(category.getParamText())) {
            throw new RuntimeException("参数名称不能为空");
        }
        if (StringUtils.isEmpty(category.getIsVolid())) {
            throw new RuntimeException("是否有效不能为空");
        }
        //判断是否存在
        Category param = findParamCriteria(category);
        if (param != null) {
            throw new RuntimeException("该参数已存在,不允许添加");
        }
        categoryDao.createParam(category);
    }

    /**
     * 查询单个参数
     *
     * @param category
     * @return
     */
    @Override
    public Category findParamCriteria(Category category) {
        return categoryDao.findParamCriteria(category);
    }

    /**
     * 修改
     *
     * @param category
     */
    @Override
    public void modifyParam(Category category) {
        categoryDao.modifyParam(category);
    }

    /**
     * 树查询
     *
     * @param category
     * @return
     */
    @Override
    public List<CategoryTree> findParamTree(Category category) {
        List<CategoryTree> categoryTrees = new ArrayList<>();
        List<Map> categoryList = categoryDao.findCategoryListCriteria(category.getCategoryName());
        if (categoryList != null && categoryList.size() != 0) {
            categoryList.stream().forEach(map -> {
                if (map.get("category_type") != null && map.get("category_type").toString() != "") {
                    CategoryTree categoryTree = new CategoryTree();
                    categoryTree.setCategoryId("A" + map.get("category_type").toString());
                    categoryTree.setCategoryType(Integer.parseInt(map.get("category_type").toString()));
                    categoryTree.setParamText(map.get("category_name") == null ? "" : map.get("category_name").toString());

                    //通过参数类型序号查询参数
                    List<Category> categories = categoryDao.findParamsByCategoryType(map.get("category_type").toString(),category.getParamText(),category.getIsVolid());
                    categoryTree.setParams(categories);
                    categoryTrees.add(categoryTree);
                }
            });
        }
        return categoryTrees;
    }

    @Override
    public List<Category> getParamsNew(String categoryName) {
        return categoryDao.getParamsNew(categoryName);
    }

    @Override
    public List<Category> getSettlementPeriods(String categoryName) {
        return categoryDao.getSettlementPeriods(categoryName);
    }
}

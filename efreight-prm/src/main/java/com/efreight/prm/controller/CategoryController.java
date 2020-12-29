package com.efreight.prm.controller;

import com.efreight.common.security.util.MessageInfo;
import com.efreight.prm.entity.Category;
import com.efreight.prm.entity.CategoryTree;
import com.efreight.prm.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 有条件查询参数列表
     *
     * @param category
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/findParamListCriteria")
    public Map<String, Object> findParamListCriteria(Category category, Integer currentPage, Integer pageSize) {
        return categoryService.findParamListCriteria(category, currentPage, pageSize);
    }

    @GetMapping
    public MessageInfo findParamTree(Category category) {
        try {
            List<CategoryTree> result = categoryService.findParamTree(category);
            return MessageInfo.ok(result);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除一个参数
     *
     * @param category
     */
    @RequestMapping("/deleteParam")
    public void deleteParam(Category category) {
        categoryService.deleteParam(category);
    }

    /**
     * 查询所有参数类型
     *
     * @return
     */
    @RequestMapping("/findCategoryList")
    public List<Map> findCategoryList() {
        List<Map> list = categoryService.findCategoryList();
        ArrayList<Map> resultList = new ArrayList<>();
        for (Map map : list
        ) {
            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("categoryId", map.get("category_type"));
            resultMap.put("categoryName", map.get("category_name"));
            resultList.add(resultMap);
        }
        return resultList;
    }

    /**
     * 创建一个参数
     *
     * @param category
     */
    @RequestMapping("/createParam")
    public void createParam(Category category) {
        try {
            categoryService.createParam(category);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 查询一个参数
     *
     * @param category
     * @return
     */
    @RequestMapping("/findParamCriteria")
    public Category findParamCriteria(Category category) {

        return categoryService.findParamCriteria(category);
    }

    /**
     * 修改参数
     *
     * @param category
     */
    @RequestMapping("/modifyParam")
    public void modifyParam(Category category) {
        categoryService.modifyParam(category);
    }

    /**
     * 新业务范畴查询
     * @param categoryName
     * @return
     */
    @GetMapping("/paramsNew")
    public MessageInfo getParamsNew(String categoryName) {
        try {
            List<Category> params = categoryService.getParamsNew(categoryName);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * EQ查询
     * @param categoryName
     * @return
     */
    @GetMapping("/getSettlementPeriods")
    public MessageInfo getSettlementPeriods(String categoryName) {
        try {
            List<Category> params = categoryService.getSettlementPeriods(categoryName);
            return MessageInfo.ok(params);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}

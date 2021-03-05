package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.AfVPrmCategoryMapper;
import com.efreight.afbase.dao.CategoryMapper;
import com.efreight.afbase.entity.AfVPrmCategory;
import com.efreight.afbase.entity.Category;
import com.efreight.afbase.entity.CategoryTree;
import com.efreight.afbase.service.CategoryService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 参数表 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
@AllArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
	private final AfVPrmCategoryMapper vCategoryMapper;
    @Override
    public IPage<Category> getPage(Page page, Category category) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(category.getCategoryName())) {
            queryWrapper.like("category_name", "%" + category.getCategoryName() + "%");
        }
        if (StrUtil.isNotBlank(category.getParamText())) {
            queryWrapper.like("param_text", "%" + category.getParamText() + "%");
        }

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<CategoryTree> getTree(Category category) {
        ArrayList<CategoryTree> trees = new ArrayList<>();
        LambdaQueryWrapper<Category> wrapper1 = Wrappers.<Category>lambdaQuery();
        if (category.getIsValid() != null) {
            wrapper1.eq(Category::getIsValid, category.getIsValid());
        }
        if (StrUtil.isNotBlank(category.getCategoryName())) {
            wrapper1.like(Category::getCategoryName, "%" + category.getCategoryName() + "%");
        }
        if (StrUtil.isNotBlank(category.getParamText())) {
            wrapper1.like(Category::getParamText, "%" + category.getParamText() + "%");
        }
        baseMapper.selectList(wrapper1.orderByAsc(Category::getCategoryType)).stream().map(Category::getCategoryType).distinct().collect(Collectors.toList()).forEach(categoryType -> {
            CategoryTree categoryTree = new CategoryTree();
            LambdaQueryWrapper<Category> wrapper2 = Wrappers.<Category>lambdaQuery();
            if (category.getIsValid() != null) {
                wrapper2.eq(Category::getIsValid, category.getIsValid());
            }
            if (StrUtil.isNotBlank(category.getCategoryName())) {
                wrapper2.like(Category::getCategoryName, "%" + category.getCategoryName() + "%");
            }
            if (StrUtil.isNotBlank(category.getParamText())) {
                wrapper2.like(Category::getParamText, "%" + category.getParamText() + "%");
            }
            List<Category> params = baseMapper.selectList(wrapper2.eq(Category::getCategoryType, categoryType).orderByAsc(Category::getParamRanking));
            categoryTree.setParamText(params.get(0).getCategoryName());
            categoryTree.setCategoryType(params.get(0).getCategoryType());
            categoryTree.setParams(params);
            categoryTree.setId("a" + params.get(0).getCategoryType());
            trees.add(categoryTree);
        });
        return trees;
    }

    @Override
    public List<Category> getParams(String categoryName) {
        if (StrUtil.isBlank(categoryName)) {
            throw new RuntimeException("参数类型名称不能为空");
        }
        if("分区".equals(categoryName)){
            return baseMapper.selectList(Wrappers.<Category>lambdaQuery().eq(Category::getCategoryName, categoryName).eq(Category::getIsValid, true).orderByAsc(Category::getParamText));
        }else{
            return baseMapper.selectList(Wrappers.<Category>lambdaQuery().eq(Category::getCategoryName, categoryName).eq(Category::getIsValid, true).orderByDesc(Category::getParamText));
        }
    }

    @Override
    public List<Category> findCategory(String categoryName) {
        if(StrUtil.isBlank(categoryName)){
            throw new RuntimeException("参数类型名称不能为空");
        }
        LambdaQueryWrapper queryWrapper = Wrappers.<Category>lambdaQuery()
                .eq(Category::getCategoryName, categoryName)
                .eq(Category::getIsValid, true)
                .orderByAsc(Category::getParamRanking);
        return baseMapper.selectList(queryWrapper);
    }

	@Override
	public List<AfVPrmCategory> getAfVPrmCategory(String categoryName) {
		return vCategoryMapper.getAfVPrmCategory(categoryName);
	}

    @Override
    public List<AfVPrmCategory> queryCategoryByCategoryType(Integer categoryType) {
        return vCategoryMapper.queryCategoryByCategoryType(categoryType);
    }

    @Override
    public List<AfVPrmCategory> findDocBusinessScope() {
        return this.vCategoryMapper.findDocBusinessScope();
    }
}

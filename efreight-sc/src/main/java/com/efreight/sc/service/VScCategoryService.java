package com.efreight.sc.service;

import com.efreight.sc.entity.view.VScCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface VScCategoryService extends IService<VScCategory> {

    List<VScCategory> getListByCategoryName(String categoryName);
}

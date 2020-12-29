package com.efreight.sc.service;

import com.efreight.sc.entity.VLcCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-22
 */
public interface VLcCategoryService extends IService<VLcCategory> {

    List<VLcCategory> getList(String categoryName);
}

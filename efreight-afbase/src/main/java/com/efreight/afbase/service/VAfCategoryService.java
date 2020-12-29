package com.efreight.afbase.service;

import com.efreight.afbase.entity.view.VAfCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-28
 */
public interface VAfCategoryService extends IService<VAfCategory> {

    List<VAfCategory> getList(String categoryName);
    List<VAfCategory> getscList(String categoryName);
}

package com.efreight.sc.service;

import com.efreight.sc.entity.VIoCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
public interface VIoCategoryService extends IService<VIoCategory> {

    List<VIoCategory> getList(String categoryName);
}

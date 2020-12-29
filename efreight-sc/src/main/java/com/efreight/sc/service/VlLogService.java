package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.VlLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * VL 派車單操作日志 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface VlLogService extends IService<VlLog> {

    void insert(VlLog vlLog);

    IPage getPage(Page page, VlLog vlLog);
}

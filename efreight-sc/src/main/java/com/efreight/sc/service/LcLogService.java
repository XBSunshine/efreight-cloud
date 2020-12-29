package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.LcLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * LC 订单操作日志 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcLogService extends IService<LcLog> {

    void insert(LcLog lcLog);

    IPage getPage(Page page, LcLog lcLog);
}

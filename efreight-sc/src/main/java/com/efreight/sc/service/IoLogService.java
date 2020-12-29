package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.IoLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * IO 订单操作日志 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoLogService extends IService<IoLog> {

    void insert(IoLog ioLog);

    IPage getPage(Page page, IoLog ioLog);
}

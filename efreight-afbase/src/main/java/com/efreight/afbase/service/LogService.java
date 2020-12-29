package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.LogBean;

public interface LogService extends IService<LogBean> {
    IPage<LogBean> getPage(Page page, LogBean logBean);

    void saveLog(LogBean logBean);

    void modifyForDeleteInbound(LogBean logBean);
}

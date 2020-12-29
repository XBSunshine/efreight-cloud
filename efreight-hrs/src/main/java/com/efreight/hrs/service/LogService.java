package com.efreight.hrs.service;

import com.efreight.hrs.entity.Log;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface LogService extends IService<Log> {
	IPage<Log> getLogList(Page page, Log bean);
	public void doSave(Log bean);

}

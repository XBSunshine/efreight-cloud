package com.efreight.sc.service;

import com.efreight.sc.entity.Log;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 基础信息 操作日志 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface LogService extends IService<Log> {
	void saveLog(Log logBean);
}

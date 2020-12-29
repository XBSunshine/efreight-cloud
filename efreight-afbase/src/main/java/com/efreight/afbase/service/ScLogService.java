package com.efreight.afbase.service;

import com.efreight.afbase.entity.ScLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * AF 基础信息 操作日志 服务类
 * </p>
 *
 * @author qipm
 * @since 2020-03-09
 */
public interface ScLogService extends IService<ScLog> {
	void saveLog(ScLog logBean);
}

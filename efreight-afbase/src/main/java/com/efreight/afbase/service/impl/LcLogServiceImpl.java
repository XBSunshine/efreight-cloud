package com.efreight.afbase.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.LcLogMapper;
import com.efreight.afbase.entity.LcLog;
import com.efreight.afbase.service.LcLogService;

/**
 * <p>
 * LC 订单操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
public class LcLogServiceImpl extends ServiceImpl<LcLogMapper, LcLog> implements LcLogService {

}

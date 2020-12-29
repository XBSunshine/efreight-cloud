package com.efreight.afbase.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.TcLogMapper;
import com.efreight.afbase.entity.TcLog;
import com.efreight.afbase.service.TcLogService;

/**
 * <p>
 * TC 订单操作日志 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@Service
public class TcLogServiceImpl extends ServiceImpl<TcLogMapper, TcLog> implements TcLogService {

}

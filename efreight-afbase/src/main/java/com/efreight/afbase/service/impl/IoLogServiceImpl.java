package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.IoLog;
import com.efreight.afbase.dao.IoLogMapper;
import com.efreight.afbase.service.IoLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * IO 订单操作日志 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-18
 */
@Service
public class IoLogServiceImpl extends ServiceImpl<IoLogMapper, IoLog> implements IoLogService {

}

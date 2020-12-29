package com.efreight.sc.service.impl;

import com.efreight.sc.entity.TcLog;
import com.efreight.sc.dao.TcLogMapper;
import com.efreight.sc.service.TcLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

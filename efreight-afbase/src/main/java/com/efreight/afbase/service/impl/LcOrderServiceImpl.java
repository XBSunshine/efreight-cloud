package com.efreight.afbase.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.LcOrderMapper;
import com.efreight.afbase.entity.LcOrder;
import com.efreight.afbase.service.LcOrderService;

/**
 * <p>
 * LC 订单管理 LC陆运订单 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
@Service
public class LcOrderServiceImpl extends ServiceImpl<LcOrderMapper, LcOrder> implements LcOrderService {

}

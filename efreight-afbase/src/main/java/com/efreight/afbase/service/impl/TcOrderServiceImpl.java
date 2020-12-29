package com.efreight.afbase.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.TcOrderMapper;
import com.efreight.afbase.entity.TcOrder;
import com.efreight.afbase.service.TcOrderService;

import lombok.AllArgsConstructor;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 服务实现类
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
@Service
@AllArgsConstructor
public class TcOrderServiceImpl extends ServiceImpl<TcOrderMapper, TcOrder> implements TcOrderService {


}


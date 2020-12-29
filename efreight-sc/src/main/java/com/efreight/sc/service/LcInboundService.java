package com.efreight.sc.service;

import com.efreight.sc.entity.LcInbound;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * LC 陆运订单： 操作出重表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-28
 */
public interface LcInboundService extends IService<LcInbound> {

    List<LcInbound> view(Integer orderId);

    void insert(LcInbound lcInbound);

    void modify(LcInbound lcInbound);

    void delete(Integer inboundId,String rowUuid);
}

package com.efreight.sc.service;

import com.efreight.sc.entity.OrderShipInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface OrderShipInfoService extends IService<OrderShipInfo> {

    List<OrderShipInfo> getList();
    IPage getPageList(Page page,OrderShipInfo info);
    void deleteInfoById(Integer shipInfoId);
    OrderShipInfo queryOne(Integer shipInfoId);
    Boolean  addInfo(OrderShipInfo info);
    Boolean  doUpdate(OrderShipInfo info);
}

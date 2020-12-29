package com.efreight.afbase.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.ShipperConsignee;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.exportExcel.ShipperConsigneeExcel;

/**
 * <p>
 * AF 基础信息 收发货人 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2019-10-09
 */
public interface ShipperConsigneeService extends IService<ShipperConsignee> {

    IPage<ShipperConsignee> getPage(Page page, ShipperConsignee shipperConsignee);
    IPage<ShipperConsignee> getPage2(Page page, ShipperConsignee shipperConsignee);
    List<ShipperConsignee> queryList(ShipperConsignee shipperConsignee);

    ShipperConsignee view(Integer scId);

    void modify(ShipperConsignee shipperConsignee);

    void cancel(Integer scId);

    IPage<ShipperConsignee> selectToPage(Page page, ShipperConsignee shipperConsignee);

    String searchCityName(String cityCode);
    String searchNationalName(String nationCode);

    List<ShipperConsigneeExcel> queryListForExcel(ShipperConsignee shipperConsignee);
}

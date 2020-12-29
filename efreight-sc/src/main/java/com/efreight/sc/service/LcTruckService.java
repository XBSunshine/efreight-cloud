package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.sc.entity.LcTruck;
import com.efreight.sc.entity.view.LcTruckExcel;

import java.util.List;

/**
 * <p>
 * LC  车辆管理 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface LcTruckService extends IService<LcTruck> {

    List<LcTruck> getList();

    void insert(LcTruck lcTruck);

    /**
     * 分页查询数据
     * @param page
     * @param lcTruck
     * @return
     */
    IPage<LcTruck> getPage(Page page, LcTruck lcTruck);

    /**
     * 删除信息
     * @param truckId 数据ID
     * @return
     */
    int delete(Integer truckId);

    /**
     * 根据ID修改数据
     * @param lcTruck
     * @return
     */
    int update(LcTruck lcTruck);

    /**
     * 根据企业ID和车牌号查询数据
     * @param orgId 企业ID
     * @param truckNumber 车牌号
     * @return
     */
    LcTruck findByTruckNumber(Integer orgId, String truckNumber);

    /**
     * 查询导出数据
     * @param lcTruck
     * @return
     */
    List<LcTruckExcel> queryListForExcel(LcTruck lcTruck);

    /**
     * 保存数据
     * @param lcTruck
     * @return
     */
    int saveLcTruck(LcTruck lcTruck);
}

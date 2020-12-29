package com.efreight.sc.service;

import com.efreight.sc.entity.PortMaintenance;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * CS 海运港口表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface PortMaintenanceService extends IService<PortMaintenance> {

    List<PortMaintenance> getList();
    
    IPage<PortMaintenance> getPage(Page page, PortMaintenance portMaintenance);

    /**
     * 查询港口信息
     * @param code 港口代码
     * @return
     */
    PortMaintenance getByCode(String code);

    /**
     * 保存信息
     * @param portMaintenance 数据实体类
     * @return 影响行数
     */
    int savePortMaintenance(PortMaintenance portMaintenance);

    /**
     * 根据数据ID进行修改
     * @param portMaintenance 数据，id字段必须有值
     * @return 影响行
     */
    int editById(PortMaintenance portMaintenance);

    List<PortMaintenance> search(String key);
}

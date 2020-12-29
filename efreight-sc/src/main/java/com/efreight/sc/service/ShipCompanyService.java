package com.efreight.sc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.sc.entity.ShipCompany;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 船司表 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-03
 */
public interface ShipCompanyService extends IService<ShipCompany> {

    IPage getPage(Page page, ShipCompany shipCompany);

    /**
     * 保存船司信息
     * @param shipCompany
     * @return
     */
    int saveShipCompany(ShipCompany shipCompany);

    /**
     * 编辑船司信息
     * @param shipCompany
     * @return
     */
    int editShipCompany(ShipCompany shipCompany);

    /**
     * 根据船司代码查询
     * @param code 船司代码
     * @return
     */
    ShipCompany getByCode(String code);
}

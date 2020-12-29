package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.Service;
import com.efreight.afbase.entity.VPrmCategory;
import com.efreight.afbase.entity.VPrmCategoryTree;

import java.util.List;

public interface ServiceService extends IService<Service> {
    List<VPrmCategoryTree> getList(String businessCode);
    List<VPrmCategoryTree> treeList(String businessCode);
    List<Service> queryList(String businessCode);
    List<Service> queryListAE(String businessCode,Integer orderId);

    void edit(Service service);

    List<VPrmCategoryTree> startPage();

    /**
     * 业务范畴
     * @return
     */
    List<VPrmCategory> businessScope();

    /**
     * 删除服务
     * @param serviceId
     * @return
     */
    int delete(Integer serviceId);
}

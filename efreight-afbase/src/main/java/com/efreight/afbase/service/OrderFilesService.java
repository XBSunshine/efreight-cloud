package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.OrderFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * AF 订单管理 出口订单附件 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-12
 */
public interface OrderFilesService extends IService<OrderFiles> {

    IPage getPage(Page page, OrderFiles orderFiles);

    void insert(OrderFiles orderFiles);

    void insertBatch(OrderFiles orderFiles);

    void delete(Integer orderFilesId,String businessScope);

    List<OrderFiles> getList(Integer orderId,String businessScope);
    List<OrderFiles> getListByWhere(OrderFiles orderFiles);
    

    void showFile(Integer orderFilesId,String businessScope,Integer isDisplay);

    void insertBatchForAF(List<OrderFiles> orderFilesList);
}

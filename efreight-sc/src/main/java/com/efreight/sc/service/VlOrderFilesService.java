package com.efreight.sc.service;

import com.efreight.sc.entity.VlOrderFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * TC 订单管理 订单附件 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
public interface VlOrderFilesService extends IService<VlOrderFiles> {

    List<VlOrderFiles> getList(Integer orderId);

    void insert(VlOrderFiles vlOrderFiles);

    void modifty(VlOrderFiles vlOrderFiles);

    void delete(Integer orderFileId);
}

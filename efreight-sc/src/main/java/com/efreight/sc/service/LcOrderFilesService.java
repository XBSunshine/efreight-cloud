package com.efreight.sc.service;

import com.efreight.sc.entity.LcOrderFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * LC 订单管理 订单附件 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-21
 */
public interface LcOrderFilesService extends IService<LcOrderFiles> {

    List<LcOrderFiles> getList(Integer orderId);

    void insert(LcOrderFiles lcOrderFiles);

    void modifty(LcOrderFiles lcOrderFiles);

    void delete(Integer orderFileId);

    List<LcOrderFiles> getListByOrderFileIds(String orderFileIds);
}

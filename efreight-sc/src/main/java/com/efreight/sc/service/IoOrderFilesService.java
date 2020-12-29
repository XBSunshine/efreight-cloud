package com.efreight.sc.service;

import com.efreight.sc.entity.IoOrderFiles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * IO 订单管理 其他业务订单附件 服务类
 * </p>
 *
 * @author xiaobo
 * @since 2020-09-17
 */
public interface IoOrderFilesService extends IService<IoOrderFiles> {

    List<IoOrderFiles> getList(Integer orderId);

    void insert(IoOrderFiles ioOrderFiles);

    void modifty(IoOrderFiles ioOrderFiles);

    void delete(Integer orderFileId);

    List<IoOrderFiles> getListByOrderFileIds(String orderFileIds);
}

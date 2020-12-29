package com.efreight.afbase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.AfOrderIdentify;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  鉴定证书服务类
 * </p>
 *
 * @author mayt
 * @since 2020-10-12
 */
public interface AfOrderIdentifyService extends IService<AfOrderIdentify> {

    List<AfOrderIdentify> getAfOrderIdentifyList(Integer orderId);

    AfOrderIdentify getAfOrderIdentify(Integer orderId);

    boolean saveAfOrderIdentify(AfOrderIdentify afOrderIdentify);

    boolean deleteAfOrderIdentify(Integer orderIdentifyId)  throws Exception;

    boolean declare(AfOrderIdentify afOrderIdentify) throws Exception;

    boolean declare(Integer orderIdentifyId) throws Exception;

    boolean deleteDeclare(Integer orderIdentifyId) throws Exception;

    boolean audit(Integer originalSyscode, String auditName) throws Exception;

}

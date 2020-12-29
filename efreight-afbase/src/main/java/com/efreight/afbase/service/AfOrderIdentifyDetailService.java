package com.efreight.afbase.service;

import com.efreight.afbase.entity.AfOrderIdentifyDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author mayt
 * @since 2020-10-13
 */
public interface AfOrderIdentifyDetailService extends IService<AfOrderIdentifyDetail> {
    List<AfOrderIdentifyDetail> getAfOrderIdentifiesDetailList(Integer masterid);
}

package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.route.AfAwbRouteTrackAwb;

import java.util.List;

public interface AfAwbRouteTrackAwbService extends IService<AfAwbRouteTrackAwb> {

    /**
     * 根据主单号查询轨迹信息
     * @param awbNumber 主单号
     * @return
     */
    List<AfAwbRouteTrackAwb> getByAwbNumber(String awbNumber);
}

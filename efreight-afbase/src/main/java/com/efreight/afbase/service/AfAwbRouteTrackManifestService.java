package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.route.AfAwbRouteTrackManifest;

import java.util.List;

/**
 * @author lc
 * @date 2020/5/6 14:07
 */
public interface AfAwbRouteTrackManifestService extends IService<AfAwbRouteTrackManifest> {

    /**
     * 获取舱单轨迹信息
     * @param awbNumber 主单号
     * @param businessScope 业务哉
     * @return
     */
    List<AfAwbRouteTrackManifest> getByAwbNumber(String awbNumber, String businessScope);

    /**
     * 操作看板
     * @param awbNumber 主单号
     * @param hawbNumber 分音号
     * @param businessScope 业务哉
     * @return
     */
    List<AfAwbRouteTrackManifest> operationLookList(String awbNumber, String hawbNumber, String businessScope);

}

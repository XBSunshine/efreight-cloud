package com.efreight.afbase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.route.AfAwbRouteHawb;

/**
 * @author lc
 * @date 2020/9/16 14:12
 */
public interface AfAwbRouteHawbService extends IService<AfAwbRouteHawb> {

    /**
     * 根据主单号和分单号查询数据
     * @param awbNumber 主单号
     * @param hawbNumber 分单号
     * @return
     */
    AfAwbRouteHawb find(String awbNumber, String hawbNumber);

    /**
     * 保存数据，如果主单号和分单号不存在时，才进行保存
     * @param afAwbRouteHawb
     * @return
     */
    int insert(AfAwbRouteHawb afAwbRouteHawb);
}

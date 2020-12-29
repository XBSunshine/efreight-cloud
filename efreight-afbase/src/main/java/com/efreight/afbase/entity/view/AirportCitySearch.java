package com.efreight.afbase.entity.view;

import lombok.Data;

/**
 * 城市搜索类
 * @author lc
 * @date 2020/10/14 14:20
 */
@Data
public class AirportCitySearch {
    /**
     * 城市代码
     */
    private String cityCode;
    /**
     * 城市英文名
     */
    private String cityNameEn;
    /**
     * 城市中文名称
     */
    private String cityNameCn;
    /**
     * 国哀中文名称
     */
    private String nationNameCn;
}

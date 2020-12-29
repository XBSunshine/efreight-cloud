package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 机场搜索结果实体类
 * @author lc
 * @date 2020/7/30 11:05
 */
@Data
public class AirportSearch implements Serializable {

    /**
     * 机场代码
     */
    private String apCode;
    /**
     * 机场英文名
     */
    private String apNameEn;
    /**
     * 机场中文名
     */
    private String apNameCn;
}

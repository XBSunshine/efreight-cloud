package com.efreight.afbase.entity.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 机场国家搜索结果
 * @author lc
 * @date 2020/7/31 16:12
 */
@Data
public class AirportCountrySearch implements Serializable {
    /**
     * 国家代码
     */
    private String nationCode;
    /**
     * 国家英文名
     */
    private String nationNameEn;
    /**
     * 国家中文名
     */
    private String nationNameCn;
}

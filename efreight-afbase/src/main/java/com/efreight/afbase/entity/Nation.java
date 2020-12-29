package com.efreight.afbase.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_nation")
public class Nation implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 国家代码
     */

    private String nationCode;

    /**
     * 国家名称
     */
    private String nationName;

    /**
     * 国家英文名称
     */
    private String nationEname;

    /**
     * 洲
     */
    private String nationContinent;

    /**
     * 是否生效
     */
    private Boolean nationStatus;


}

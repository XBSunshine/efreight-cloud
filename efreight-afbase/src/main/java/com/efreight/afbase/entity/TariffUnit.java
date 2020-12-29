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
 * AF 关税税则：单位
 * </p>
 *
 * @author xiaobo
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("af_tariff_unit")
public class TariffUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unitCode;

    private String unitName;


}

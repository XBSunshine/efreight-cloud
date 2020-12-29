package com.efreight.afbase.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * AF 关税税则
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TariffDetailsCIQ implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 税则编码
     */
    private String productCode;

    /**
     * CIQ代码
     */
    private String ciqCode;

    /**
     * CIQ名称
     */
    private String ciqName;
}

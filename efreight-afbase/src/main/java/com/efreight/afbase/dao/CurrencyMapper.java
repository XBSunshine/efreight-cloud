package com.efreight.afbase.dao;

import com.efreight.afbase.entity.Currency;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.view.VCurrencyRate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 币种 Mapper 接口
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface CurrencyMapper extends BaseMapper<Currency> {

    @Select("select currency_code,currency_rate from af_V_currency_rate where org_id=#{orgId}")
    List<VCurrencyRate> getCurrentListByOrgId(@Param("orgId") Integer orgId);
}

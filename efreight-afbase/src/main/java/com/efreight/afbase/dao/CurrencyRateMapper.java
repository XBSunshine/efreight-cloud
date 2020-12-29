/*
 *   
 * Author: zhanghw
 */

package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.Currency;
import com.efreight.afbase.entity.CurrencyRate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 汇率维护
 *
 * @author zhanghw
 * @date 2019-08-30 17:42:34
 */
public interface CurrencyRateMapper extends BaseMapper<CurrencyRate> {

    @Select({"<script>",
            "SELECT ",
            "	A.currency_code currencyCode",
            "	,A.currency_name AS currencyName",
            "	,A.currency_sign currencySign",

            "FROM af_currency A ",
            "WHERE 1=1",
            "<when test='currencyCode!=null and currencyCode!=\"\"'>",
            " AND A.currency_code =  #{currencyCode}",
            "</when>",

            "</script>"})
    Currency getCurrencyByCode( @Param("currencyCode") String currencyCode);

}

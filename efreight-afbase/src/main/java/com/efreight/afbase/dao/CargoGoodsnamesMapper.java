package com.efreight.afbase.dao;

import com.efreight.afbase.entity.CargoGoodsnames;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2020-12-17
 */
public interface CargoGoodsnamesMapper extends BaseMapper<CargoGoodsnames> {

	@Select({"<script>",
        "select",
            "* from af_cargo_goodsnames",
        "where order_id=#{bean.orderId}",
        "<when test='bean.goodsCnnames!=null and bean.goodsCnnames!=\"\"'>",
        " and goods_cnnames like  \"%\"#{bean.goodsCnnames}\"%\"",
        "</when>",
		"</script>"})
    List<CargoGoodsnames> querylist(@Param("bean") CargoGoodsnames bean);
}

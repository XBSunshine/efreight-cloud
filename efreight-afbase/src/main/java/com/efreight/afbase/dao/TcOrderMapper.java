package com.efreight.afbase.dao;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.TcOrder;

/**
 * <p>
 * TC 订单管理 TE、TI 订单 Mapper 接口
 * </p>
 *
 * @author caiwd
 * @since 2020-07-13
 */
public interface TcOrderMapper extends BaseMapper<TcOrder> {
	@Select("select UUID()")
    String getUuid();

}

package com.efreight.afbase.dao;

import com.efreight.afbase.entity.TariffDetails;
import com.efreight.afbase.entity.TariffDetailsCIQ;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 * AF 关税税则 Mapper 接口
 * </p>
 *
 * @author qipm
 * @since 2020-05-20
 */
public interface TariffDetailsMapper extends BaseMapper<TariffDetails> {

	@Select({"<script>",
		"select * from (",
		"SELECT",
		"A.*",
		",B.declare_elements ",
		"FROM af_tariff_details A",
		"LEFT JOIN (",
		"SELECT",
		"duty_paragraph,GROUP_CONCAT(CONCAT(serial_number,'.',NAME)) AS declare_elements",
		"FROM af_tariff_declare_elements",
		"GROUP BY duty_paragraph",
		") AS B ON A.product_code = B.duty_paragraph",
		"WHERE 1=1",
        "<when test='bean.productCode!=null and bean.productCode!=\"\"'>",
        " AND A.product_code like  \"%\"#{bean.productCode}\"%\"",
        "</when>",
        "<when test='bean.productName!=null and bean.productName!=\"\"'>",
        " AND A.product_name like  \"%\"#{bean.productName}\"%\"",
        "</when>",
       " LIMIT 0,50",
       ") aaa",
        "</script>"})
	IPage<TariffDetails> getListPage2(Page page, @Param("bean") TariffDetails bean);
	@Select({"<script>",
		"select * from (",
		"SELECT",
		"A.*",
		"FROM af_tariff_details A",
		"WHERE 1=1",
		"<when test='bean.productCode!=null and bean.productCode!=\"\"'>",
		" AND A.product_code like  \"%\"#{bean.productCode}\"%\"",
		"</when>",
		"<when test='bean.productName!=null and bean.productName!=\"\"'>",
		" AND A.product_name like  \"%\"#{bean.productName}\"%\"",
		"</when>",
		" LIMIT 0,50",
		") aaa",
	"</script>"})
	IPage<TariffDetails> getListPage(Page page, @Param("bean") TariffDetails bean);
	@Select({"<script>",
		"select CONCAT(product_code,ciq_code) ciq_code,ciq_name",
		"from af_tariff_details_ciq",
		"where product_code=#{bean.productCode}",
	"</script>"})
	List<TariffDetailsCIQ> getCIQ(@Param("bean") TariffDetailsCIQ bean);
}

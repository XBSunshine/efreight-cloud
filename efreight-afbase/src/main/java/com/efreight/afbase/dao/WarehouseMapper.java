package com.efreight.afbase.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.efreight.afbase.entity.Warehouse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


public interface WarehouseMapper extends BaseMapper<Warehouse> {
	@Select({"<script>",
		" SELECT ",
		" *",
		" FROM af_warehouse",
		"	where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and customs_code=#{bean.customsCode} and warehouse_status = 1\n",
	"</script>"})
	List<Warehouse> getWarehouseList(@Param("bean") Warehouse bean);

	@Select({"<script>",
			" SELECT\n" +
					"\tshipper_template_name \n" +
					"FROM\n" +
					"\taf_warehouse_letter \n" +
					"WHERE\n" +
					"\twarehouse_letter_id =#{shipperTemplate}\n",
			"</script>"})
	String getTemplateNameById(@Param("shipperTemplate") Integer shipperTemplate);

	@Select({"<script>",
			" SELECT ",
			" *",
			" FROM af_warehouse",
			"	where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and warehouse_code=#{bean.warehouseCode} and warehouse_status = 1 \n",
			"<when test='bean.warehouseId != null'>",
			" AND warehouse_id != #{bean.warehouseId}",
			"</when>",
			"</script>"})
	List<Warehouse> ifExistWarehouseCode(@Param("bean") Warehouse bean);

	@Select({"<script>",
			" SELECT ",
			" *",
			" FROM af_warehouse",
			"	where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and upper(warehouse_name_cn)=upper(#{bean.warehouseNameCn}) and warehouse_status = 1 \n",
			"<when test='bean.warehouseId != null'>",
			" AND warehouse_id != #{bean.warehouseId}",
			"</when>",
			"</script>"})
	List<Warehouse> ifExistWarehouseNameCn(@Param("bean") Warehouse bean);

	@Select({"<script>",
			" SELECT ",
			" *",
			" FROM af_warehouse",
			"	where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and warehouse_code=#{bean.warehouseCode} and warehouse_status = 1 \n",
			"</script>"})
	List<Warehouse> ifExistWarehouseCode1(@Param("bean") Warehouse bean);

	@Select({"<script>",
			" SELECT ",
			" warehouse_id, warehouse_code,concat(warehouse_name_cn,ap_code) as warehouse_name_cn",
			" FROM af_warehouse",
			"	where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and upper(warehouse_name_cn)=upper(#{bean.warehouseNameCn}) and warehouse_status = 1 \n",
			"order by warehouse_code",
			"</script>"})
	List<Warehouse> ifExistWarehouseNameCn1(@Param("bean") Warehouse bean);

	@Select({"<script>",
			" SELECT ",
			" *",
			" FROM af_warehouse ",
			"where org_id = #{bean.orgId} and business_scope = #{bean.businessScope} and warehouse_status = 1 " ,
			/*"<when test='bean.warehouseCode!=null and bean.warehouseCode!=''>",
			" AND (upper(warehouse_code) like \"%\"upper(#{bean.warehouseCode})\"%\" or upper(warehouse_name_cn) like \"%\"upper(#{bean.warehouseCode})\"%\" or upper(ap_code) like \"%\"upper(#{bean.warehouseCode})\"%\")",
			"</when>",*/
			"</script>"})
	List<Warehouse> getWarehouseListByQuery(@Param("bean") Warehouse bean);
}

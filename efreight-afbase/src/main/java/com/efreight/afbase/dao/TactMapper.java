package com.efreight.afbase.dao;

import com.efreight.afbase.entity.exportExcel.TactExcel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.efreight.afbase.entity.Tact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Map;


public interface TactMapper extends BaseMapper<Tact> {
	
	@Select({"<script>",
		   " SELECT tact.tact_id tactId, tact.carrier_code carrierCode,",
//		   "carrier.carrier_prefix carrierPrefix,",
		" tact.departure_station departureStation,",
//		" airport1.ap_name_cn  departureStationName,",
		" tact.arrival_station arrivalStation,",
		" tact.begin_date,",
		" tact.org_id,",
		" ifnull(tact.end_date,'2099-12-31') AS end_date,",
//		"airport2.ap_name_cn arrivalStationName,",
		" tact.tact_m tactM,tact.tact_n tactN,",
		" tact.tact_45 tact45,tact.tact_100 tact100,",
		" tact.tact_300 tact300,tact.tact_500 tact500,",
		" tact.tact_700 tact700,tact.tact_1000 tact1000,tact.tact_2000 tact2000,tact.tact_3000 tact3000,tact.tact_5000 tact5000,tact.tact_remark ",
		" FROM af_tact tact",
		" LEFT JOIN af_carrier carrier ON tact.carrier_code=carrier.carrier_code",
//		" LEFT JOIN af_airport airport1 ON tact.departure_station=airport1.ap_code",
//		" LEFT JOIN af_airport airport2 ON tact.arrival_station=airport2.ap_code",
		   " WHERE 1=1",
		    "<when test='bean.carrierCode!=null and bean.carrierCode!=\"\"'>",
		    " AND (tact.carrier_code = #{bean.carrierCode} or carrier.carrier_prefix=#{bean.carrierCode})",
		    "</when>",
/*		    "<when test='bean.carrierCode==null or bean.carrierCode==\"\"'>",
		    " AND (tact.carrier_code =''  or tact.carrier_code is null)",
		    "</when>",*/
		    "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
		    " AND tact.departure_station in (${bean.departureStation})",
		    "</when>",
		    "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND arrival_station in (${bean.arrivalStation})",
		    "</when>",
		    "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",          
//		    " AND a.create_time &gt;= #{createTimeBegin}",
		    " AND tact.begin_date &lt;= #{bean.createTimeBegin}",
		    "</when>",
		    "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",            
//		    " AND a.create_time &lt;= #{bean.createTimeEnd}",
		    " AND ifnull(tact.end_date,'2099-12-31') &gt;= #{bean.createTimeBegin}",
		    "</when>",
		    "<when test='bean.orgId!=null'>",            
		    " AND tact.org_id=#{bean.orgId}",
		    "</when>",
		    "<when test='bean.dataSourceDef!=null and bean.dataSourceDef!=\"\"'>",            
		    " AND tact.org_id in (#{bean.dataSourceDef},1) ",
		    "</when>",
		    "ORDER BY tact.carrier_code asc,tact.org_id desc",
		  "</script>"})
	IPage<Tact> getList(Page page,@Param("bean") Tact bean);

	@Select({"<script>",
			"SELECT\n" +
					"\tB.ap_code \n" +
					"FROM\n" +
					"\taf_airport A\n" +
					"\tINNER JOIN af_airport B ON A.city_code = B.city_code \n" +
					"\tAND B.ap_code != A.ap_code \n" +
					"WHERE\n" +
					"\tA.ap_code = #{arrivalStation}",
			"</script>"})
	List<String> getCity(@Param("arrivalStation") String arrivalStation);
	
	@Select({"<script>",
		    "SELECT ",
		    " A.ap_code,A.city_code ",
		    " FROM",
		    " af_airport A",
		    " WHERE",
		    " A.ap_code = #{station} or A.city_code=#{station}",
		"</script>"})
    List<Map<String,String>> getAirportOrCity(@Param("station") String station);
	
	@Select({"<script>",
	    "SELECT ",
	    " appid,api_type ",
	    " FROM",
	    " tom_api_config ",
	    " WHERE",
	    " appid=#{appid} and enable=1 and api_type='SAAS_TACT'",
	"</script>"})
    List<Map<String,String>> getAppid(@Param("appid") String appid);

	@Select({"<script>",
			" SELECT ",
			" tact.tact_m tactM,tact.tact_n tactN,",
			" tact.tact_45 tact45,tact.tact_100 tact100,",
			" tact.tact_300 tact300,tact.tact_500 tact500,",
			" tact.tact_700 tact700,tact.tact_1000 tact1000,tact.tact_2000 tact2000,tact.tact_3000 tact3000,tact.tact_5000 tact5000 ",
			" FROM af_tact tact",
			" LEFT JOIN af_carrier carrier ON tact.carrier_code=carrier.carrier_code",
			" WHERE 1=1",
			"<when test='bean.awbNumberPrefix!=null and bean.awbNumberPrefix!=\"\"'>",
			" AND (carrier.carrier_prefix=#{bean.awbNumberPrefix} or (tact.carrier_code =''  or tact.carrier_code is null)) ",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND tact.departure_station in (${bean.departureStation})",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND arrival_station in (${bean.arrivalStation})",
			"</when>",
			"<when test='bean.flightDate!=null and bean.flightDate!=\"\"'>",
			" AND tact.begin_date &lt;= #{bean.flightDate}",
			"</when>",
			"<when test='bean.flightDate!=null and bean.flightDate!=\"\"'>",
			" AND ifnull(tact.end_date,'2099-12-31') &gt;= #{bean.flightDate}",
			"</when>",
			"<when test='bean.orgId!=null'>",
			" AND tact.org_id=#{bean.orgId}",
			"</when>",
			"LIMIT 1",
			"</script>"})
	Tact getTactForBillMake(@Param("bean") Tact bean);

	@Select({"<script>",
			" SELECT\n" +
					"  '私有数据' AS dataSource,\n" +
					"\ttact.carrier_code carrierCode,\n" +
					"\ttact.departure_station departureStation,\n" +
					"\ttact.arrival_station arrivalStation,\n" +
					"\tSUBSTRING_INDEX(tact.begin_date, ' ',  1) AS begin_date,\n" +
					"\tifnull( SUBSTRING_INDEX(tact.end_date, ' ',  1), '2099-12-31' ) AS end_date,\n" +
					"\tifnull(tact.tact_m,'-') tactM,\n" +
					"\tifnull(tact.tact_n,'-') tactN,\n" +
					"\tifnull(tact.tact_45,'-') tact45,\n" +
					"\tifnull(tact.tact_100,'-') tact100,\n" +
					"\tifnull(tact.tact_300,'-') tact300,\n" +
					"\tifnull(tact.tact_500,'-') tact500,\n" +
					"\tifnull(tact.tact_700,'-') tact700,\n" +
					"\tifnull(tact.tact_1000,'-') tact1000,\n" +
					"\tifnull(tact.tact_2000,'-') tact2000,\n" +
					"\tifnull(tact.tact_3000,'-') tact3000,\n" +
					"\tifnull(tact.tact_5000,'-') tact5000,\n" +
					"\ttact.tact_remark  ",
			" FROM af_tact tact",
			" LEFT JOIN af_carrier carrier ON tact.carrier_code=carrier.carrier_code",
			" WHERE 1=1",
			"<when test='bean.carrierCode!=null and bean.carrierCode!=\"\"'>",
			" AND (tact.carrier_code = #{bean.carrierCode} or carrier.carrier_prefix=#{bean.carrierCode})",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND tact.departure_station in (${bean.departureStation})",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND arrival_station in (${bean.arrivalStation})",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND tact.begin_date &lt;= #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND ifnull(tact.end_date,'2099-12-31') &gt;= #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.orgId!=null'>",
			" AND tact.org_id=#{bean.orgId}",
			"</when>",
			"<when test='bean.dataSourceDef!=null and bean.dataSourceDef!=\"\"'>",
			" AND tact.org_id in (#{bean.dataSourceDef},1) ",
			"</when>",
			"ORDER BY tact.carrier_code asc,tact.org_id desc",
			"</script>"})
	List<TactExcel> queryListForExcel(@Param("bean") Tact bean);

}

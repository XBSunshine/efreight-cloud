package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Airport;
import com.efreight.afbase.entity.Nation;
import com.efreight.afbase.entity.Surcharge;
import com.efreight.afbase.entity.Tact;
import com.efreight.afbase.entity.exportExcel.SurchargeExcel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


public interface SurchargeMapper extends BaseMapper<Surcharge> {

	@Select({"<script>",
		   " SELECT\n" +
		   "\tsurcharge.*,\n" +
		   "\tCONCAT( carrier.carrier_code, '(', carrier.carrier_prefix, ')' ) AS carrier_code,\n" +
		   "\tairport.nation_code \n" +
		   "FROM\n" +
		   "\taf_awb_surcharge surcharge\n" +
		   "\tLEFT JOIN af_carrier carrier ON surcharge.carrier_id = carrier.carrier_id\n" +
		   "\tLEFT JOIN af_airport airport ON surcharge.arrival_station = airport.ap_code",
			" WHERE 1=1",
			"<when test='bean.orgId!=null'>",
			" AND surcharge.org_id = #{bean.orgId}",
			"</when>",
			"<when test='bean.carrierCode!=null and bean.carrierCode!=\"\"'>",
			" AND (carrier.carrier_code like  \"%\"#{bean.carrierCode}\"%\" or carrier.carrier_prefix like  \"%\"#{bean.carrierCode}\"%\")",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND surcharge.departure_station like  \"%\"#{bean.departureStation}\"%\"",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND (surcharge.arrival_station like  \"%\"#{bean.arrivalStation}\"%\" or (surcharge.arrival_station = '' and surcharge.routing_name = (select routing_group_name from af_airport where ap_code = #{bean.arrivalStation}))" +
					" or (surcharge.arrival_station = '' and surcharge.arrival_nation_code = (select nation_code from af_airport where ap_code = #{bean.arrivalStation})))",
			"</when>",
			"<when test='bean.surchargeCode!=null and bean.surchargeCode!=\"\"'>",
			" AND surcharge.surcharge_code like  \"%\"#{bean.surchargeCode}\"%\"",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND surcharge.begin_date &lt;= #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND ifnull(surcharge.end_date,'2099-12-31') &gt;= #{bean.createTimeBegin}",
			"</when>",
			"ORDER BY carrier.carrier_code,surcharge.departure_station,surcharge.awb_sort asc",
		  "</script>"})
	IPage<Surcharge> getList(Page page, @Param("bean") Surcharge bean);

	@Select({"<script>",
			"SELECT ap_code,CONCAT('(',ap_code,')',ap_name_cn) AS ap_name_cn FROM af_airport\n" +
					"ORDER BY ap_code",
			"</script>"})
	List<Airport> getDepartureStationList();

	@Select({"<script>",
			"SELECT nation_code,CONCAT('(',nation_code,') ',MAX(nation_name_cn)) AS nation_name,MAX(nation_name_en) AS nation_ename FROM af_airport\n" +
					"GROUP BY nation_code\n" +
					"ORDER BY nation_code",
			"</script>"})
	List<Nation> getNationCodesList();

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
			"SELECT\n" +
				"\tsurcharge.surcharge_code,\n" +
				"\tsurcharge.charge_method,\n" +
				"\tsurcharge.unit_price,\n" +
				"\tsurcharge.charge_min,\n" +
				"\tsurcharge.charge_max \n" +
				"FROM\n" +
				"\taf_awb_surcharge surcharge\n" +
				"\tLEFT JOIN af_carrier carrier ON surcharge.carrier_id = carrier.carrier_id \n" +
				"WHERE\n" +
				"\t1 = 1 \n" +
				"\tAND surcharge.org_id = #{bean.orgId} \n" +
				"\tAND carrier.carrier_prefix = #{bean.awbNumberPrefix} \n" +
				"\tAND ( surcharge.departure_station LIKE  \"%\"#{bean.departureStation}\"%\" OR surcharge.departure_station IS NULL OR surcharge.departure_station = '' ) \n" +
				"\tAND ( surcharge.arrival_station LIKE  \"%\"#{bean.arrivalStation}\"%\"  or (surcharge.arrival_station = '' and surcharge.routing_name = (select routing_group_name from af_airport where ap_code = #{bean.arrivalStation}))" +
					"or (surcharge.arrival_station = '' and surcharge.arrival_nation_code = (select nation_code from af_airport where ap_code = #{bean.arrivalStation}))) \n" +
				"\tAND surcharge.begin_date &lt;= #{bean.flightDate} AND ifnull( surcharge.end_date, '2099-12-31' ) &gt;= #{bean.flightDate}" +
			    "\tORDER BY surcharge.awb_sort",
			"</script>"})
	List<Surcharge> getSurchargeForBillMake(@Param("bean") Surcharge bean);

	@Select({"<script>",
			" SELECT\n" +
					"\tsurcharge.*,\n" +
					"\tCONCAT( carrier.carrier_code, '(', carrier.carrier_prefix, ')' ) AS carrier_code,\n" +
					"\tairport.nation_code \n" +
					"FROM\n" +
					"\taf_awb_surcharge surcharge\n" +
					"\tLEFT JOIN af_carrier carrier ON surcharge.carrier_id = carrier.carrier_id\n" +
					"\tLEFT JOIN af_airport airport ON surcharge.arrival_station = airport.ap_code",
			" WHERE 1=1",
			"<when test='bean.orgId!=null'>",
			" AND surcharge.org_id = #{bean.orgId}",
			"</when>",
			"<when test='bean.carrierCode!=null and bean.carrierCode!=\"\"'>",
			" AND (carrier.carrier_code like  \"%\"#{bean.carrierCode}\"%\" or carrier.carrier_prefix like  \"%\"#{bean.carrierCode}\"%\")",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND surcharge.departure_station like  \"%\"#{bean.departureStation}\"%\"",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND surcharge.arrival_station like  \"%\"#{bean.arrivalStation}\"%\"",
			"</when>",
			"<when test='bean.surchargeCode!=null and bean.surchargeCode!=\"\"'>",
			" AND surcharge.surcharge_code like  \"%\"#{bean.surchargeCode}\"%\"",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND surcharge.begin_date &lt;= #{bean.createTimeBegin}",
			"</when>",
			"<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",
			" AND ifnull(surcharge.end_date,'2099-12-31') &gt;= #{bean.createTimeBegin}",
			"</when>",
			"ORDER BY carrier.carrier_code,surcharge.departure_station,surcharge.awb_sort asc",
			"</script>"})
	List<SurchargeExcel> queryListForExcel(@Param("bean") Surcharge bean);

}

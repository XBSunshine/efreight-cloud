package com.efreight.afbase.dao;

import com.efreight.afbase.entity.AwbNumber;
import com.efreight.afbase.entity.FlightDetail;
import com.efreight.afbase.entity.FlightMerge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.efreight.afbase.entity.Flight;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;


public interface FlightMapper extends BaseMapper<Flight> {

	@Select({"<script>",
		   "SELECT ",
			 "	A.flight_number flightNumber",
			 "	,CASE B.week_num",
			 "	 WHEN 1 THEN '星期一'",
			 "	 WHEN 2 THEN '星期二'",
			 "	 WHEN 3 THEN '星期三'",
			 "	 WHEN 4 THEN '星期四'",
			 "	 WHEN 5 THEN '星期五'",
			 "	 WHEN 6 THEN '星期六'",
			 "	 WHEN 7 THEN '星期日'",
			 "	 ELSE '' END weekNum",
			 "	,A.departure_station departureStation",
			 "	,B.transit_station transitStation,B.arrival_station arrivalStation",
			 "	,CASE B.aircraft_type_pc ",
			 "	 WHEN 'k' THEN '客机' ",
			 "	 WHEN 'h' THEN '货机'",
			 "	 ELSE '' END AS aircraftTypePc",
			 "	 ,CASE B.aircraft_type_bn ",
			 "	 WHEN 'k' THEN '宽体' ",
			 "	 WHEN 'z' THEN '窄体'",
			 "	 ELSE '' END AS aircraftTypeBn",
			 "	 ,B.takeoff_time takeoffTime",
			 "	 ,B.arrival_time arrivalTime",
			 "FROM af_flight A ",
			 "INNER JOIN af_flight_voyage B ON A.flight_id=B.flight_id",
			 "WHERE 1=1",
			"<when test='bean.flightNumber!=null and bean.flightNumber!=\"\"'>",
		    " AND A.flight_number like  \"%\"#{bean.flightNumber}\"%\"",
		    "</when>",
		    "<when test='bean.flightDate!=null'>",
			"  AND A.begin_date <![CDATA[ <= ]]> #{bean.flightDate} AND IFNULL(A.end_date,'2099-12-31') <![CDATA[ >= ]]> #{bean.flightDate}",
		    " AND B.week_num = WEEKDAY(#{bean.flightDate})+1 ",
		    "</when>",
		    "<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
		    " AND A.departure_station = #{bean.departureStation}",
		    "</when>",
		    
		    "<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
		    " AND ( ",
//		    + "CONCAT(B.arrival_station,B.transit_station) LIKE \"%\"#{bean.arrivalStation}\"%\"",
		     "B.arrival_station = #{bean.arrivalStation} or B.transit_station = #{bean.arrivalStation}",
			    "<when test='bean.isTrue'>",
			    " OR B.arrival_station IN (",
			    " 		SELECT B.ap_code ",
			    " 		FROM af_airport A ",
			    " 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			    " 		WHERE A.ap_code=#{bean.arrivalStation} ",
			    " 	)",
			    " OR B.transit_station IN (",
			    " 		SELECT B.ap_code ",
			    " 		FROM af_airport A ",
			    " 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			    " 		WHERE A.ap_code=#{bean.arrivalStation} ",
			    " 	)",
			    "</when>",
		    ")",
		    "</when>",
		    " order by A.flight_number,B.week_num,A.departure_station,B.arrival_station,B.takeoff_time",
		  "</script>"})
	IPage<Flight> getListPage(Page page,@Param("bean") Flight bean);

	@Select({"<script>",
			"select * from (  ",
			"select ",
			"	 A.flight_number flightNumber",
            "    ,GROUP_CONCAT(DISTINCT B.week_num) as weekNum",
			"	 ,MAX(A.departure_station) as departureStation",
			"	 ,MAX(B.transit_station) as transitStation,MAX(B.arrival_station) as arrivalStation",
			"	,CASE MAX(B.aircraft_type_pc) ",
			"	 WHEN 'k' THEN '客机' ",
			"	 WHEN 'h' THEN '货机'",
			"	 ELSE '' END AS aircraftTypePc",
			"	 ,CASE MAX(B.aircraft_type_bn) ",
			"	 WHEN 'k' THEN '宽体' ",
			"	 WHEN 'z' THEN '窄体'",
			"	 ELSE '' END AS aircraftTypeBn",
			"	 ,MAX(B.takeoff_time) as takeoffTime",
			"	 ,MAX(B.arrival_time) as arrivalTime",
			"	 ,0 as panelLevel",
			"FROM af_flight A ",
			"INNER JOIN af_flight_voyage B ON A.flight_id=B.flight_id",
			"WHERE 1=1",

			"<when test='bean.flightNumber!=null and bean.flightNumber!=\"\"'>",
			" AND A.flight_number like  \"%\"#{bean.flightNumber}\"%\"",
			"</when>",

			"<when test='bean.flightDate!=null'>",
			"  AND A.begin_date <![CDATA[ <= ]]> #{bean.flightDate} AND IFNULL(A.end_date,'2099-12-31') <![CDATA[ >= ]]> #{bean.flightDate}",
			" AND B.week_num = WEEKDAY(#{bean.flightDate})+1 ",
			"</when>",

			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND A.departure_station = #{bean.departureStation}",
			"</when>",

			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND ( ",
			"B.arrival_station = #{bean.arrivalStation} or B.transit_station = #{bean.arrivalStation}",
			"<when test='bean.isTrue'>",
			" OR B.arrival_station IN (",
			" 		SELECT B.ap_code ",
			" 		FROM af_airport A ",
			" 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			" 		WHERE A.ap_code=#{bean.arrivalStation} ",
			" 	)",
			" OR B.transit_station IN (",
			" 		SELECT B.ap_code ",
			" 		FROM af_airport A ",
			" 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			" 		WHERE A.ap_code=#{bean.arrivalStation} ",
			" 	)",
			"</when>",
			")",
			"</when>",

			"group by A.flight_number",
			"UNION ALL",
			"select ",
			"	A.flight_number flightNumber",
			"	,CASE B.week_num",
			"	 WHEN 1 THEN '星期一'",
			"	 WHEN 2 THEN '星期二'",
			"	 WHEN 3 THEN '星期三'",
			"	 WHEN 4 THEN '星期四'",
			"	 WHEN 5 THEN '星期五'",
			"	 WHEN 6 THEN '星期六'",
			"	 WHEN 7 THEN '星期日'",
			"	 ELSE '' END weekNum",
			"	,A.departure_station departureStation",
			"	,B.transit_station transitStation,B.arrival_station arrivalStation",
			"	,CASE B.aircraft_type_pc ",
			"	 WHEN 'k' THEN '客机' ",
			"	 WHEN 'h' THEN '货机'",
			"	 ELSE '' END AS aircraftTypePc",
			"	 ,CASE B.aircraft_type_bn ",
			"	 WHEN 'k' THEN '宽体' ",
			"	 WHEN 'z' THEN '窄体'",
			"	 ELSE '' END AS aircraftTypeBn",
			"	 ,B.takeoff_time takeoffTime",
			"	 ,B.arrival_time arrivalTime",
			"	 ,1 as panelLevel",
			"FROM af_flight A ",
			"INNER JOIN af_flight_voyage B ON A.flight_id=B.flight_id",
			"WHERE 1=1",

			"<when test='bean.flightNumber!=null and bean.flightNumber!=\"\"'>",
			" AND A.flight_number like  \"%\"#{bean.flightNumber}\"%\"",
			"</when>",

			") as T",
			"</script>"})
	List<FlightMerge> getFlightList(@Param("bean") Flight bean);

    @Select({"<script>",
            "SELECT ",
            "	A.flight_number flightNumber",
			"	,MAX(A.flight_id) flightId",
            "	 ,GROUP_CONCAT( DISTINCT B.week_num ) AS weekNum",
            "	,MAX(A.departure_station) departureStation",
            "	,MAX(B.transit_station) transitStation,MAX(B.arrival_station) arrivalStation",
            "	,CASE MAX(B.aircraft_type_pc) ",
            "	 WHEN 'k' THEN '客机' ",
            "	 WHEN 'h' THEN '货机'",
            "	 ELSE '' END AS aircraftTypePc",
            "	 ,CASE MAX(B.aircraft_type_bn) ",
            "	 WHEN 'k' THEN '宽体' ",
            "	 WHEN 'z' THEN '窄体'",
            "	 ELSE '' END AS aircraftTypeBn",
            "	 ,MAX(B.takeoff_time) takeoffTime",
            "	 ,MAX(B.arrival_time) arrivalTime",
			"	 ,MIN(B.cutoff_time) cutoffTime",
			"	 ,MAX(A.begin_date) beginDate",
			"	 ,MAX(A.end_date) endDate",
			"	 ,MAX(A.editor_name) editorName",
			"	 ,MAX(A.edit_time) editTime",
			"	 ,0 as isSign",
            "FROM af_flight A ",
            "INNER JOIN af_flight_voyage B ON A.flight_id=B.flight_id",
            "WHERE 1=1",
			"<when test='bean.flightNumber!=null and bean.flightNumber!=\"\"'>",
			" AND flight_number like  \"%\"#{bean.flightNumber}\"%\"",
			"</when>",
			"<when test='bean.flightDate!=null'>",
			"  AND begin_date <![CDATA[ <= ]]> #{bean.flightDate} AND IFNULL(end_date,'2099-12-31') <![CDATA[ >= ]]> #{bean.flightDate}",
			"</when>",
			"<when test='bean.departureStation!=null and bean.departureStation!=\"\"'>",
			" AND departure_station = #{bean.departureStation}",
			"</when>",
			"<when test='bean.arrivalStation!=null and bean.arrivalStation!=\"\"'>",
			" AND ( ",
			"arrival_station = #{bean.arrivalStation} or transit_station = #{bean.arrivalStation}",
			"<when test='bean.isTrue'>",
			" OR arrival_station IN (",
			" 		SELECT B.ap_code ",
			" 		FROM af_airport A ",
			" 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			" 		WHERE A.ap_code=#{bean.arrivalStation} ",
			" 	)",
			" OR transit_station IN (",
			" 		SELECT B.ap_code ",
			" 		FROM af_airport A ",
			" 		INNER JOIN af_airport B ON A.city_code=B.city_code AND B.ap_code!=A.ap_code",
			" 		WHERE A.ap_code=#{bean.arrivalStation} ",
			" 	)",
			"</when>",
			")",
			"</when>",
            "group by A.flight_number" ,
			"  having 1=1" ,
            "</script>"})
    IPage<Flight> getFlightListPage(Page page,@Param("bean") Flight bean);

    @Select({"<script>",
            "SELECT ",
            "	A.flight_number flightNumber",
			"	,A.flight_id flightId",
            "	,CASE B.week_num",
            "	 WHEN 1 THEN '星期一'",
            "	 WHEN 2 THEN '星期二'",
            "	 WHEN 3 THEN '星期三'",
            "	 WHEN 4 THEN '星期四'",
            "	 WHEN 5 THEN '星期五'",
            "	 WHEN 6 THEN '星期六'",
            "	 WHEN 7 THEN '星期日'",
            "	 ELSE '' END weekNum",
            "	,A.departure_station departureStation",
            "	,B.transit_station transitStation,B.arrival_station arrivalStation",
            "	,B.aircraft_type_remark",
            "	,CASE B.aircraft_type_pc ",
            "	 WHEN 'k' THEN '客机' ",
            "	 WHEN 'h' THEN '货机'",
            "	 ELSE '' END AS aircraftTypePc",
            "	 ,CASE B.aircraft_type_bn ",
            "	 WHEN 'k' THEN '宽体' ",
            "	 WHEN 'z' THEN '窄体'",
            "	 ELSE '' END AS aircraftTypeBn",
            "	 ,B.takeoff_time takeoffTime",
            "	 ,B.arrival_time arrivalTime",
			"	 ,B.cutoff_time cutoffTime",
			"	 ,1 as isSign",
            "FROM af_flight A ",
            "INNER JOIN af_flight_voyage B ON A.flight_id=B.flight_id",
            "WHERE 1=1",
            "<when test='bean.flightNumber!=null and bean.flightNumber!=\"\"'>",
            " AND A.flight_number like  \"%\"#{bean.flightNumber}\"%\"",
            "</when>",
			"<when test='bean.flightDate!=null'>",
			"  AND begin_date <![CDATA[ <= ]]> #{bean.flightDate} AND IFNULL(end_date,'2099-12-31') <![CDATA[ >= ]]> #{bean.flightDate}",
			"</when>",
			" order by B.week_num",
            "</script>"})
    List<FlightDetail> getFlightDetailByFlightNumber(@Param("bean") Flight bean);

	@Insert("insert into af_flight_voyage \n"
			+ " ( flight_id,week_num,aircraft_type_pc,aircraft_type_bn,aircraft_type_remark,transit_station,arrival_station,cutoff_time,takeoff_time,arrival_time) \n"
			+ "	 values (#{bean.flightId},#{bean.weekNum},#{bean.aircraftTypePc},#{bean.aircraftTypeBn},#{bean.aircraftTypeRemark},#{bean.transitStation},#{bean.arrivalStation},#{bean.cutoffTime}"
			+ " ,#{bean.takeoffTime},#{bean.arrivalTime})\n")
	void insertFlightDatail(@Param("bean") FlightDetail bean);

	@Delete("delete from af_flight_voyage \n"
			+ " where flight_id=#{flightId} \n")
	void deleteFlightDetailById(@Param("flightId") String flightId);

	@Select("select COUNT(1) from af_carrier \n"
			+ " where carrier_code=#{carrierCode} \n")
	Integer isHavedFlight(@Param("carrierCode") String carrierCode);

	@Select("select COUNT(1) from af_airport \n"
			+ " where ap_code=#{departureStation} \n")
	Integer isHavedDepartureStation(@Param("departureStation") String departureStation);

	@Select("select COUNT(1) from af_airport \n"
			+ " where ap_code=#{transitStation} \n")
	Integer isHavedTransitStation(@Param("transitStation") String transitStation);

	@Select("select COUNT(1) from af_airport \n"
			+ " where ap_code=#{arrivalStation} \n")
	Integer isHavedArrivalStation(@Param("arrivalStation") String arrivalStation);

}

package com.efreight.afbase.dao;

import com.efreight.afbase.entity.OrderInquiry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.exportExcel.OrderInquiryQuotationExcel;
import com.efreight.afbase.entity.procedure.SettleStatement;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * AF 询价单 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-05-26
 */
public interface OrderInquiryMapper extends BaseMapper<OrderInquiry> {

    @Select("select UUID()")
    String getUuid();

    @Select("select inquiry_agent_name_short as inquiryAgentName,cast(inquiry_agent_id as char) as inquiryAgentId from prm_inquiry_agent where inquiry_id in (${inquiryAgentIds})")
    List<Map<String, String>> selectInquiryAgentByIds(@Param("inquiryAgentIds") String inquiryAgentIds);

    @Select("select * from prm_coop where coop_id=#{inquiryAgentId}")
    Map<String, String> viewCoop(@Param("inquiryAgentId") String inquiryAgentId);

    @Select("select CONCAT(cast(IFNULL(count(1),0) AS CHAR),' / ',CAST(IFNULL(SUM(g.count),0) AS CHAR)) inquiryPlan  from (select count(1) as count from af_order_Inquiry_quotation a where a.order_inquiry_id=#{orderInquiryId} and a.is_valid = 1 and a.org_id = #{orgId} GROUP BY a.quotation_company_name,a.quotation_contacts,a.quotation_end_date) g")
    Map<String, String> selectInquiryPlan(@Param("orderInquiryId") Integer orderInquiryId, @Param("orgId") Integer orgId);

    @Select("select contacts_name name,email from prm_coop_contacts where contacts_id in (SELECT substring_index(substring_index( a.booking_contacts_id, ',', b.help_topic_id + 1 ), ',',- 1) FROM (select booking_contacts_id from prm_inquiry_agent a where a.inquiry_id in (${inquryAgentIds})) a JOIN mysql.help_topic b ON b.help_topic_id < (length(a.booking_contacts_id) - length(REPLACE(a.booking_contacts_id, ',', '' )) + 1))")
    List<Map<String, String>> getInquryAgentContactList(@Param("inquryAgentIds") String inquryAgentIds, @Param("orgId") Integer orgId);

    @Select({"<script>",
            "SELECT a.inquiry_id,a.inquiry_agent_id,a.inquiry_agent_name_short,a.booking_contacts_id,p.coop_code from prm_inquiry_agent a",
            " left join prm_coop p on p.coop_id=a.inquiry_agent_id ",
            "	where a.org_id=#{orgId} and  a.business_scope='AE' and a.is_valid=1",
            " <when test='dep!=null and dep!=\"\"'>",
            " AND locate(#{dep},a.departure_station)>0 ",
            "</when>",
            "</script>"})
    List<Map> getInquryAgentDepList(@Param("orgId") Integer orgId, @Param("dep") String dep);

    @Options(statementType = StatementType.CALLABLE)
    @ResultMap({"orderInquiryQuotationExcel1", "orderInquiryQuotationExcel2"})
    @Select({"<script>",
            "CALL af_P_order_Inquiry_quotation_print(#{orderInquiryId},#{orgId})\n",
            "</script>"})
    List<List<OrderInquiryQuotationExcel>> queryInquiryQuotationExcel(@Param("orgId") Integer orgId, @Param("orderInquiryId") Integer orderInquiryId);

    @Select("CALL af_P_order_inquiry_create_four_YC(#{orgid},#{creatorid})")
    void createFourYCWhenInquiry(@Param("orgid") Integer orgid,@Param("creatorid") Integer creatorid);

    @Select("select * from prm_coop where org_id=#{orgid} and coop_code in ('YFTCAN','YFTPEK','YFTSHA','YFTXMN')")
    List<Map> getFourYCWhenInquiry(@Param("orgid") Integer orgId);
}

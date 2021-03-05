package com.efreight.hrs.dao;

import com.efreight.hrs.entity.doc.DocQuery;
import com.efreight.hrs.entity.doc.DocView;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;

/**
 * @author lc
 * @date 2021/1/21 17:23
 */
public interface DocMapper{

    @Select("CALL hrs_P_doc(#{query.orgId}, #{query.businessScope}, #{query.docName}, #{query.number}, #{query.orderCode}, " +
            "#{query.customerNumber},#{query.uploadTimeStart},#{query.uploadTimeEnd},#{query.dateTimeStart},#{query.dateTimeEnd}," +
            "#{query.seller},#{query.customerService},#{query.operator},#{query.customer},#{query.docType},#{query.docOperator}," +
            "#{query.current}, #{query.size}, #{query.total, mode=OUT, jdbcType=INTEGER})")
    @Options(statementType = StatementType.CALLABLE)
    List<DocView> listDocView(@Param("query") DocQuery docQuery);
}

package com.efreight.afbase.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.AwbSubscription;
import com.efreight.afbase.entity.view.SubscribeVO;
import com.efreight.common.security.vo.OrgServiceMealConfigVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * AF 运单号 我的订阅 Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2020-06-29
 */
public interface AwbSubscriptionMapper extends BaseMapper<AwbSubscription> {

    /**
     * 企业附加服务 剩余量
     * @param orgId
     * @param serviceType
     * @return
     */
    @Select("select " +
            "service_number_max as serviceNumberMax , " +
            "service_number_used as serviceNumberUsed, " +
            "(service_number_max - service_number_used) as remaining  " +
            "from hrs_org_service_meal_config " +
            "where org_id = #{orgId}" +
            " and service_type = #{serviceType}")
    OrgServiceMealConfigVo orgAdditionalServiceRemaining(@Param("orgId") Integer orgId, @Param("serviceType") String serviceType);

    /**
     * 个人订阅量
     * @param orgId
     * @return
     */
    @Select("SELECT COUNT(1) FROM \n" +
            "(\n" +
            "\tSELECT org_id,1 AS FCOUNT\n" +
            "\tFROM af_awb_subscription\n" +
            "\tWHERE org_id= #{orgId}\n" +
            "\tAND DATE_FORMAT(create_time,'%Y-%m') = DATE_FORMAT(NOW(),'%Y-%m')\n" +
            "\tGROUP BY org_id,awb_number,hawb_number\n" +
            ") AS T\n")
    int countSubscriptHistory(Integer orgId);

    /**
     * 修改企业附加服务使用量
     * @param orgId
     * @param serviceType
     * @param useCount
     * @return
     */
    @Update("update hrs_org_service_meal_config set service_number_used = #{useCount} where org_id = #{orgId} and service_type = #{serviceType}")
    int updateOrgAdditionalServiceRemaining(@Param("orgId") Integer orgId,
                                            @Param("serviceType") String serviceType,
                                            @Param("useCount") Integer useCount);

    @Select("SELECT\n" +
            "DATE_FORMAT(B.create_time, '%Y-%m') AS date," +
            "B.awb_number,\n" +
            "B.hawb_number,\n" +
            "CASE B.business_scope\n" +
            "WHEN 'AE' THEN '空运出口'\n" +
            "WHEN 'AI' THEN '空运进口'\n" +
            "ELSE '' END AS business_scope,\n" +
            "DATE_FORMAT(B.create_time, '%Y-%m-%d %T') AS create_time,\n" +
            "U.user_name AS creator,\n" +
            "B.create_ip\n" +
            "FROM\n" +
            "(\n" +
            "SELECT\n" +
            "MIN(A.awb_subscription_id) AS awb_subscription_id\n" +
            "FROM af_awb_subscription A\n" +
            "WHERE A.org_id = #{orgId}\n" +
            "AND A.subscription_from='货物追踪'\n" +
            "GROUP BY A.awb_number,A.hawb_number\n" +
            ") AS A\n" +
            "INNER JOIN af_awb_subscription B ON A.awb_subscription_id=B.awb_subscription_id\n" +
            "INNER JOIN hrs_user U ON U.user_id = B.user_id\n" +
            "WHERE DATE_FORMAT(B.create_time, '%Y-%m') =#{date}\n" +
            "ORDER BY B.create_time DESC")
    IPage<SubscribeVO> pageSubscribe(Page page, @Param("orgId")Integer orgId, @Param("date") String date);


    @Select("SELECT\n" +
            "DATE_FORMAT(B.create_time, '%Y-%m') AS date," +
            "B.awb_number,\n" +
            "B.hawb_number,\n" +
            "CASE B.business_scope\n" +
            "WHEN 'AE' THEN '空运出口'\n" +
            "WHEN 'AI' THEN '空运进口'\n" +
            "ELSE '' END AS business_scope,\n" +
            "DATE_FORMAT(B.create_time, '%Y-%m-%d %T') AS create_time,\n" +
            "U.user_name AS creator,\n" +
            "B.create_ip\n" +
            "FROM\n" +
            "(\n" +
            "SELECT\n" +
            "MIN(A.awb_subscription_id) AS awb_subscription_id\n" +
            "FROM af_awb_subscription A\n" +
            "WHERE A.org_id = #{orgId}\n" +
            "AND A.subscription_from='货物追踪'\n" +
            "GROUP BY A.awb_number,A.hawb_number\n" +
            ") AS A\n" +
            "INNER JOIN af_awb_subscription B ON A.awb_subscription_id=B.awb_subscription_id\n" +
            "INNER JOIN hrs_user U ON U.user_id = B.user_id\n" +
            "WHERE DATE_FORMAT(B.create_time, '%Y-%m') =#{date}\n" +
            "ORDER BY B.create_time DESC")
    List<SubscribeVO> exportSubscribe(@Param("orgId")Integer orgId, @Param("date") String date);
}

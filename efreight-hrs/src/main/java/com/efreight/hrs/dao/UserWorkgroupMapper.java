package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.UserWorkgroup;
import com.efreight.hrs.pojo.org.workgroup.UserListBean;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupExport;
import com.efreight.hrs.pojo.org.workgroup.UserWorkgroupQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lc
 * @date 2020/10/15 14:05
 */
public interface UserWorkgroupMapper extends BaseMapper<UserWorkgroup> {

    @Select("<script> " +
            "SELECT \n" +
            "UW.*\n" +
            "FROM (\n" +
            "SELECT IFNULL(HWC.user_count,0) as user_count, W.*\n" +
            "FROM `hrs_user_workgroup` W LEFT JOIN (\n" +
            "SELECT count(HUWD.user_id) as user_count, HUW.workgroup_id " +
            "FROM hrs_user_workgroup HUW JOIN hrs_user_workgroup_detail HUWD ON HUW.workgroup_id = HUWD.workgroup_id  GROUP BY HUW.workgroup_id HAVING  HUW.workgroup_id = workgroup_id\n" +
            ") AS HWC ON W.workgroup_id = HWC.workgroup_id\n" +
            " WHERE W.org_id = #{condition.orgId} \n" +
            "<if test='condition.businessScope != null and condition.businessScope  !=\"\" '>" +
                "AND W.business_scope = #{condition.businessScope}" +
            "</if>" +
            "<if test='condition.groupName != null and condition.groupName !=\"\" '>" +
                "AND W.workgroup_name like \"%\"#{condition.groupName}\"%\" " +
            "</if>" +
            ") AS UW " +
            "<if test='condition.userName != null and condition.userName !=\"\" '>" +
                "LEFT JOIN hrs_user_workgroup_detail UWD ON UW.workgroup_id = UWD.workgroup_id JOIN hrs_user HU ON UWD.user_id = HU.user_id AND HU.user_name like \"%\"#{condition.userName}\"%\" " +
                "GROUP BY UW.workgroup_id" +
            "</if>" +
            "</script>")
    IPage<UserWorkgroup> query(Page page, @Param("condition") UserWorkgroupQuery condition);

    @Select("<script>" +
            "SELECT R.*, U.user_name, U.user_email as email, U.phone_number as phone, U.job_position, D.dept_name FROM \n" +
            "(\n" +
            "SELECT UW.*, UWD.user_id FROM `hrs_user_workgroup` UW LEFt JOIN hrs_user_workgroup_detail UWD ON UW.workgroup_id = UWD.workgroup_id \n" +
            "WHERE UW.org_id = #{condition.orgId} " +
            "<if test='condition.businessScope != null and condition.businessScope  !=\"\" '>" +
                "AND UW.business_scope = #{condition.businessScope} " +
            "</if>" +
            "<if test='condition.groupName != null and condition.groupName !=\"\" '>" +
                "AND UW.workgroup_name like \"%\"#{condition.groupName}\"%\" " +
            "</if>" +
            ") AS R \n" +
            "LEFT JOIN hrs_user U ON R.user_id = U.user_id \n" +
            "LEFT JOIN hrs_dept D ON U.dept_id = D.dept_id  \n" +
            "<if test='condition.userName != null and condition.userName !=\"\" '>" +
                "WHERE U.user_name like \"%\"#{condition.userName}\"%\" " +
            "</if>" +
            "order by R.business_scope, R.workgroup_name, U.user_name" +
            "</script>"
    )
    List<UserWorkgroupExport> exportQuery( @Param("condition") UserWorkgroupQuery condition);

    @Select("SELECT \n" +
            "HU.user_id, HU.user_name, HU.user_email, HU.phone_number, HU.job_position,HD.dept_name \n" +
            "FROM hrs_user HU LEFT JOIN hrs_dept HD ON HU.dept_id = HD.dept_id \n" +
            "WHERE HU.user_status = 1 AND HU.isadmin = 0 AND HU.org_id = #{orgId}" +
            " order by HU.user_name")
    List<UserListBean> findUser(Integer orgId);

    @Select("select " +
            "HU.user_id, HU.user_name, HU.user_email, HU.phone_number, HU.job_position, HD.dept_name\n" +
            "from hrs_user_workgroup_detail WD, hrs_user HU, hrs_dept HD\n" +
            "WHERE WD.user_id = HU.user_id AND HU.dept_id = HD.dept_id\n" +
            "AND WD.workgroup_id = #{workgroupId};")
    List<UserListBean> findByWorkgroupId(Integer workgroupId);

    @Select("SELECT\n" +
            "\tworkgroup_name,\n" +
            "\tworkgroup_id \n" +
            "FROM\n" +
            "\thrs_user_workgroup \n" +
            "WHERE\n" +
            "\torg_id = #{orgId} \n" +
            "\tAND (business_scope IS NULL OR business_scope = '' \n" +
            "\tOR business_scope = #{businessScope})\n" +
            "ORDER BY\n" +
            "\tworkgroup_name")
    List<UserWorkgroup> selectWorkgroup(@Param("businessScope") String businessScope ,@Param("orgId") Integer orgId);

    @Select({"<script>" +
            "SELECT workgroup_id FROM hrs_user_workgroup_detail where user_id = #{servicerId}" +
            "</script>"})
    List<Integer> selectWorkgroupByServicerId(Integer servicerId);
}

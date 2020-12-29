package com.efreight.hrs.dao;

import com.efreight.common.security.vo.UserVo;
import com.efreight.hrs.entity.*;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface UserMapper extends BaseMapper<User> {
    User selectByUserId(Integer userId);

    @Select("SELECT " + "	`user`.user_id, " + "	`user`.user_name," + "	`user`.`pass_word`,"
            + "	`user`.phone_number," + "	`user`.user_ename," + "	`user`.id_type," + "	`user`.org_id,"
            + "	`user`.dept_id AS deptId," + "	`user`.create_time AS ucreate_time,"
            + "	`user`.user_status AS user_status," + "	r.role_id," + "	r.role_name,"
            + "	r.create_time AS rcreate_time" + "FROM" + "	hrs_user AS `user`"
            + "	LEFT JOIN hrs_user_role AS ur ON ur.user_id = `user`.user_id"
            + "	LEFT JOIN hrs_role AS r ON r.role_id = ur.role_id " + "WHERE" + "`user`.user_status = 1 and 	`user`.login_name =  #{userName}")
    UserVo getUserVoByUsername(@org.springframework.data.repository.query.Param("username") String username);

    @Select("update hrs_user g set g.leave_date=#{leaveDate},g.leave_reason=#{leaveReason},g.user_status=0 where g.user_id=#{userId} and g.org_id=#{orgId}")
    void leave(@Param("userId") String userId, @Param("leaveDate") String leaveDate, @Param("leaveReason") String leaveReason, @Param("orgId") Integer orgId);

    @Select("update hrs_user g set g.leave_date=null,g.leave_reason=null,g.user_status=1 where g.user_id=#{userId} and g.org_id=#{orgId}")
    void resume(@Param("userId") String userId, @Param("orgId") Integer orgId);

    @Select("update hrs_user g set g.blacklist_date=#{blackDate},g.blacklist_reason=#{blackReason},g.user_status=0 where g.user_id=#{userId} and g.org_id=#{orgId}")
    void black(@Param("userId") String userId, @Param("blackDate") String blackDate, @Param("blackReason") String blackReason, @Param("orgId") Integer orgId);

    @Select({"<script>" +
            "select a.user_id userId,a.login_name loginName,a.job_number jobNumber,a.user_email userEmail,a.phone_number phoneNumber," +
            " a.user_name userName,a.user_ename userEname,a.id_type idType,a.id_number idNumber,a.user_sex userSex," +
            " a.user_birthday userBirthday,a.hire_date hireDate,a.employment_type employmentType,a.job_position jobPosition," +
            " a.leave_date leaveDate,a.leave_reason leaveReason,a.blacklist_date blacklistDate,a.blacklist_reason blacklistReason," +
            " b.user_name creatorName,a.create_time createTime,c.dept_name deptName," +
            " case when a.user_status=1 then '生效' when a.user_status=0 then '失效' else '-' end userStatus" +
            " from hrs_user a " +
            " left join hrs_user b on a.creator_id=b.user_id  " +
            " left join hrs_dept c on a.dept_id=c.dept_id" +
            " where " +
            " a.org_id = #{orgId} and a.isadmin='0'" +
            "<when test='userName!=null and userName!=\"\"'>" +
            "    and a.user_name like  \"%\"#{userName}\"%\"" +
            "</when>" +
            "<when test='employmentType!=null and employmentType!=\"\"'>" +
            "    and a.employment_type = #{employmentType}" +
            "</when>" +
            "<when test='userEmail!=null and userEmail!=\"\"'>" +
            "    and a.user_email = #{userEmail}" +
            "</when>" +
            "<when test='phoneNumber!=null and phoneNumber!=\"\"'>" +
            "    and a.phone_number = #{phoneNumber}" +
            "</when>" +
            "<when test='userBirthdayStart!=null'>" +
            "    <![CDATA[and a.user_birthday >= #{userBirthdayStart}]]>" +
            "</when>" +
            "<when test='userBirthdayEnd!=null'>" +
            "    <![CDATA[and a.user_birthday <= #{userBirthdayEnd}]]>" +
            "</when>" +
            "<when test='hireDateStart!=null'>" +
            "    <![CDATA[and a.hire_date >= #{hireDateStart}]]>" +
            "</when>" +
            "<when test='hireDateEnd!=null'>" +
            "    <![CDATA[and a.hire_date <= #{hireDateEnd}]]>" +
            "</when>" +
            "<when test='leaveDateStart!=null'>" +
            "    <![CDATA[and a.leave_date >= #{leaveDateStart}]]>" +
            "</when>" +
            "<when test='leaveDateEnd!=null'>" +
            "    <![CDATA[and a.leave_date <= #{leaveDateEnd}]]>" +
            "</when>" +
            "<when test='blacklistDateStart!=null'>" +
            "    <![CDATA[and a.blacklist_date >= #{blacklistDateStart}]]>" +
            "</when>" +
            "<when test='blacklistDateEnd!=null'>" +
            "    <![CDATA[and a.blacklist_date <= #{blacklistDateEnd}]]>" +
            "</when>" +
            "<when test='editTimeStart!=null'>" +
            "    <![CDATA[and a.edit_time >= #{editTimeStart}]]>" +
            "</when>" +
            "<when test='editTimeEnd!=null'>" +
            "    <![CDATA[and a.edit_time <= #{editTimeEnd}]]>" +
            "</when>" +
            "<when test='deptCode!=null and deptCode !=\"\"'>" +
            "    and a.dept_id in(select dept_id from hrs_dept where org_id=#{orgId} and dept_code like #{deptCode}\"%\")" +
            "</when>" +
            "<when test='ifBlack1!=null and ifBlack1 !=\"\" and ifBlack1 == \"0\"'>" +
            "    and a.blacklist_date is null" +
            "</when>" +
            "<when test='ifBlack1!=null and ifBlack1 !=\"\" and ifBlack1 == \"1\"'>" +
            "    and a.blacklist_date is not null" +
            "</when>" +
            "<when test='ifLeave1!=null and ifLeave1 !=\"\" and ifLeave1 == \"0\"'>" +
            "    and a.leave_date is null" +
            "</when>" +
            "<when test='ifLeave1!=null and ifLeave1 !=\"\" and ifLeave1 == \"1\"'>" +
            "    and a.leave_date is not null" +
            "</when>" +
            " order by a.user_id desc" +
            "</script>"})
    List<UserExcel> queryListForExcel(UserVo user);

    @Select("select count(phone_number) as countPhone from hrs_user where isadmin=0 and phone_number= #{phoneNumber} ")
    Integer countByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Select("select count(phone_number) as countPhone from hrs_user where isadmin =0 and phone_number = #{phoneNumber} and user_id != #{userId}")
    Integer countByPhoneNumber1(@Param("phoneNumber") String phoneNumber,@Param("userId") Integer userId);

    @Select({"<script>" +
            "select a.user_id,b.order_finance_lock_view,b.org_code orgCode,b.org_name orgName,b.org_type orgType,b1.org_name orgVersion,a.login_name loginName,a.user_email userEmail,b.stop_date stopDate,a.order_permission orderPermission,b2.rounting_sign rountingSign,\n" +
            "\tb2.rounting_sign_business_product rountingSignBusinessProduct " +
            " from hrs_user a " +
            " left join hrs_org b on a.org_id=b.org_id  " +
            " left join hrs_org b1 on b.org_edition_id = b1.org_id  " +
            " LEFT JOIN hrs_org_order_config b2 ON a.org_id = b2.org_id AND b2.business_scope='AE'  " +
            " where " +
            " LOWER(a.login_name) = #{userEmail} and a.isadmin= 0" +
            "</script>"})
    List<UserVo> searchLoginNameAndOrgCode(UserVo user);

    @Select({"<script>" +
            "select a.user_id,b.order_finance_lock_view,b.org_code orgCode,b.org_name orgName,b.org_type orgType,b1.org_name orgVersion,a.user_email userEmail,case a.user_id when 1 then 'superAdmin' else 'admin' end as adminRole,b.stop_date stopDate,a.order_permission orderPermission,b2.rounting_sign rountingSign,\n" +
            "\tb2.rounting_sign_business_product rountingSignBusinessProduct " +
            " from hrs_user a " +
            " left join hrs_org b on a.org_id=b.org_id  " +
            " left join hrs_org b1 on b.org_edition_id = b1.org_id  " +
            " LEFT JOIN hrs_org_order_config b2 ON a.org_id = b2.org_id AND b2.business_scope='AE'  " +
            " where " +
            " a.login_name = #{loginName} and a.isadmin= 1" +
            "</script>"})
    List<UserVo> searchLoginNameAndOrgCode1(UserVo user);
    @Select({"<script>" +
            "select a.user_id,b.order_finance_lock_view,b.org_code orgCode,b.org_name orgName,b.org_type orgType,b1.org_name orgVersion,a.login_name loginName,a.user_email userEmail,b.stop_date stopDate,a.order_permission orderPermission,b2.rounting_sign rountingSign,\n" +
            "\tb2.rounting_sign_business_product rountingSignBusinessProduct " +
            " from hrs_user a " +
            " left join hrs_org b on a.org_id=b.org_id  " +
            " left join hrs_org b1 on b.org_edition_id = b1.org_id  " +
            " LEFT JOIN hrs_org_order_config b2 ON a.org_id = b2.org_id AND b2.business_scope='AE'  " +
            " where " +
            " a.phone_number = #{phoneNumber} and a.international_country_code = #{internationalCountryCode} and a.isadmin= 0" +
            "</script>"})
    List<UserVo> searchLoginNameAndOrgCode3(UserVo user);

    @Select({"<script>" +
            "SELECT\n" +
            "\tcount( user_id ) AS userCount,\n" +
            "\torg.org_user_count orgUserCount " +
            " from hrs_user us " +
            " INNER JOIN hrs_org org ON us.org_id =org.org_id  " +
            " where " +
            " us.org_id = #{orgId} AND isadmin =0" +
            "</script>"})
    User selectByOrgId(@Param("orgId") Integer orgId);

    @Select({"<script>" +
            "SELECT " +
            " a.user_id userId,a.login_name loginName,a.pass_word passWord,a.job_number jobNumber,a.user_email userEmail,a.phone_number phoneNumber, " +
            " a.user_name userName,a.user_ename userEname,a.id_type idType,a.id_number idNumber,a.user_sex userSex," +
            " a.user_birthday userBirthday,a.hire_date hireDate,a.employment_type employmentType,a.user_resume userResume,a.user_family userFamily,a.user_remarks userRemarks,a.job_position jobPosition," +
            " a.leave_date leaveDate,a.leave_reason leaveReason,a.blacklist_date blacklistDate,a.blacklist_reason blacklistReason,a.org_id orgId,a.dept_id deptId,a.user_status userStatus,a.isadmin isadmin" +
            " from hrs_user a " +
            " where " +
            " a.org_id = #{orgId} AND a.isadmin = 0 AND a.user_status = 1 AND LOWER(a.user_email) = #{userEmail}" +
            "</script>"})
    User getUserInfoByUserEmail(@Param("userEmail") String userEmail,@Param("orgId") Integer orgId);

    @Select("select count(user_email) as countEmail from hrs_user where isadmin =0 and LOWER(user_email) = #{userEmail} and user_id != #{userId}")
    Integer countByUserEmail2(@Param("userEmail") String userEmail,@Param("userId") Integer userId);

    @Select("select count(user_email) as countEmail from hrs_user where isadmin =1 and LOWER(user_email) = #{userEmail} and user_id != #{userId}")
    Integer countByUserEmail(@Param("userEmail") String userEmail,@Param("userId") Integer userId);

    @Select("select count(user_email) as countEmail from hrs_user where isadmin =0 and LOWER(user_email) = #{userEmail}")
    Integer countByUserEmail1(@Param("userEmail") String userEmail);

    @Select("select GROUP_CONCAT(coop_name) from prm_coop GROUP BY transactor_id HAVING transactor_id = #{userId}")
    String getTransactorUserByUserId(@Param("userId") String userId);
    
    @Select({"<script>" +
            "select a.user_id as id,a.user_name as name " +
            " from hrs_user a " +
            " inner join hrs_org b on a.org_id=b.org_id  " +
            " where " +
            " b.org_code = #{orgCode} and a.user_status= 1 and a.isadmin=0" +
            "</script>"})
    List<Map> searchUserByOrg(String orgCode);

    @Select({"<script>" +
            "select CASE c.dept_code WHEN '111' THEN '' ELSE c.full_name END AS deptName,a.user_name userName,CONCAT(a.international_country_code,'-',a.phone_number) phoneNumber,a.user_email userEmail" +
            " from hrs_user a " +
            " left join hrs_user b on a.creator_id=b.user_id  " +
            " left join hrs_dept c on a.dept_id=c.dept_id" +
            " where " +
            " a.org_id = #{orgId} and a.isadmin='0'" +
            "<when test='userName!=null and userName!=\"\"'>" +
            "    and a.user_name like  \"%\"#{userName}\"%\"" +
            "</when>" +
            "<when test='employmentType!=null and employmentType!=\"\"'>" +
            "    and a.employment_type = #{employmentType}" +
            "</when>" +
            "<when test='userEmail!=null and userEmail!=\"\"'>" +
            "    and a.user_email = #{userEmail}" +
            "</when>" +
            "<when test='phoneNumber!=null and phoneNumber!=\"\"'>" +
            "    and a.phone_number = #{phoneNumber}" +
            "</when>" +
            "<when test='userBirthdayStart!=null'>" +
            "    <![CDATA[and a.user_birthday >= #{userBirthdayStart}]]>" +
            "</when>" +
            "<when test='userBirthdayEnd!=null'>" +
            "    <![CDATA[and a.user_birthday <= #{userBirthdayEnd}]]>" +
            "</when>" +
            "<when test='hireDateStart!=null'>" +
            "    <![CDATA[and a.hire_date >= #{hireDateStart}]]>" +
            "</when>" +
            "<when test='hireDateEnd!=null'>" +
            "    <![CDATA[and a.hire_date <= #{hireDateEnd}]]>" +
            "</when>" +
            "<when test='leaveDateStart!=null'>" +
            "    <![CDATA[and a.leave_date >= #{leaveDateStart}]]>" +
            "</when>" +
            "<when test='leaveDateEnd!=null'>" +
            "    <![CDATA[and a.leave_date <= #{leaveDateEnd}]]>" +
            "</when>" +
            "<when test='blacklistDateStart!=null'>" +
            "    <![CDATA[and a.blacklist_date >= #{blacklistDateStart}]]>" +
            "</when>" +
            "<when test='blacklistDateEnd!=null'>" +
            "    <![CDATA[and a.blacklist_date <= #{blacklistDateEnd}]]>" +
            "</when>" +
            "<when test='editTimeStart!=null'>" +
            "    <![CDATA[and a.edit_time >= #{editTimeStart}]]>" +
            "</when>" +
            "<when test='editTimeEnd!=null'>" +
            "    <![CDATA[and a.edit_time <= #{editTimeEnd}]]>" +
            "</when>" +
            "<when test='deptCode!=null and deptCode !=\"\"'>" +
            "    and a.dept_id in(select dept_id from hrs_dept where org_id=#{orgId} and dept_code like #{deptCode}\"%\")" +
            "</when>" +
            "<when test='ifBlack1!=null and ifBlack1 !=\"\" and ifBlack1 == \"0\"'>" +
            "    and a.blacklist_date is null" +
            "</when>" +
            "<when test='ifBlack1!=null and ifBlack1 !=\"\" and ifBlack1 == \"1\"'>" +
            "    and a.blacklist_date is not null" +
            "</when>" +
            "<when test='ifLeave1!=null and ifLeave1 !=\"\" and ifLeave1 == \"0\"'>" +
            "    and a.leave_date is null" +
            "</when>" +
            "<when test='ifLeave1!=null and ifLeave1 !=\"\" and ifLeave1 == \"1\"'>" +
            "    and a.leave_date is not null" +
            "</when>" +
            " order by a.dept_id,a.user_name asc" +
            "</script>"})
    List<UserAddressExcel> queryListForAddressExcel(UserVo user);
    
    @Select({"<script>" +
            "SELECT " +
            " a.user_id,a.login_name,a.pass_word_verification,a.international_country_code,a.user_email,a.phone_number " +
            " from hrs_user a " +
            " where " +
            " a.org_id = #{orgId} AND a.isadmin = 0 AND a.international_country_code =#{phoneArea} AND a.phone_number=#{phone}" +
            "<when test='email!=null and email !=\"\"'>" +
            "  AND LOWER(a.user_email) = #{email}"+
            "</when>" +
            
            "</script>"})
    User getUserInfoByUserPhone(@Param("phone") String phone,@Param("phoneArea") String phoneArea,@Param("orgId") Integer orgId,@Param("email") String email);

    @Delete({"<script>",
            " delete  ",
            "FROM hrs_user_mail_cc ",
            "WHERE org_id=#{orgId} AND user_id=#{userId} ",
            "</script>"})
    void removeUserMailCc(@Param("orgId") Integer orgId,@Param("userId") Integer userId);

    @Insert("insert into hrs_user_mail_cc \n"
            + " ( org_id,user_id,permission_name,user_id_cc,create_time) \n"
            + "	 values (#{bean.orgId},#{bean.userId},#{bean.permissionName},#{bean.userIdCc},#{bean.createTime})"
            + " \n")
    void insetUserMailCc(@Param("bean") UserMailCc bean);

    @Select({"<script>" +
            "select a.user_id_cc " +
            " from hrs_user_mail_cc a INNER JOIN hrs_user u ON a.org_id = u.org_id \n" +
            "\tAND a.user_id_cc = u.user_id" +
            " where " +
            " a.org_id = #{orgId} and a.user_id= #{userId} and a.permission_name= #{permissionName} AND u.user_status = 1 " +
            "</script>"})
    List<Integer> getUserIdCc(@Param("orgId") Integer orgId,@Param("userId") Integer userId,@Param("permissionName") String permissionName);

    @Select({"<script>" +
            "SELECT workgroup_id FROM hrs_user_workgroup_detail where user_id = #{userId}" +
            "</script>"})
    List<Integer> getUserWorkgroupDetail(Integer userId);

    @Select("select count(*) as countUser from hrs_user where isadmin=0 and org_id= #{orgId} ")
    int countByOrgId(@Param("orgId") Integer orgId);
}

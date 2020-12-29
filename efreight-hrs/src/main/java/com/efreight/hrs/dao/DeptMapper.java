package com.efreight.hrs.dao;

import java.util.List;
import java.util.Map;

import com.efreight.hrs.entity.UserMailCc;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.efreight.hrs.entity.Dept;
import com.efreight.hrs.entity.DeptExcel;
import com.efreight.hrs.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface DeptMapper extends BaseMapper<Dept> {
	
	@Select({"<script>",
		   "SELECT hrs_dept.*,hrs_user.user_name managerName,c.actualHc FROM hrs_dept ",
		   " left join hrs_user on hrs_dept.manager_id=hrs_user.user_id ",
		   " LEFT JOIN  ",
		   " (SELECT dept_id,COUNT(1) actualHc  FROM hrs_user WHERE user_status=1 and org_id=#{org_id}",
		   " GROUP BY dept_id) c ",
		   "  ON hrs_dept.dept_id=c.dept_id ",
		   " WHERE 1=1 and LENGTH(hrs_dept.dept_code)=3",
		    " AND hrs_dept.org_id = #{org_id}",
		    "<when test='dept_status!=null'>",
		    " AND hrs_dept.dept_status = #{dept_status}",
		    "</when>",
		    "<when test='dept_code!=null and dept_code!=\"\"'>",
		    " AND hrs_dept.dept_code like  \"%\"#{dept_code}\"%\"",
		    "</when>",
		    "<when test='dept_name!=null and dept_name!=\"\"'>",            
		    " AND hrs_dept.dept_name like  \"%\"#{dept_name}\"%\"",
		    "</when>",
		  "</script>"})
	IPage<Dept> getDeptList(Page page,@Param("org_id") Integer org_id,@Param("dept_status") Boolean dept_status,@Param("dept_code") String dept_code,@Param("dept_name") String dept_name);
	
	@Select({"<script>",
		   "SELECT hrs_dept.*,hrs_user.user_name managerName,c.actualHc from 	hrs_dept ",
		   " left join hrs_user on hrs_dept.manager_id=hrs_user.user_id ",
		   " LEFT JOIN  ",
		   " (SELECT dept_id,COUNT(1) actualHc  FROM hrs_user WHERE user_status=1 and org_id=#{org_id}",
		   " GROUP BY dept_id) c ",
		   "  ON hrs_dept.dept_id=c.dept_id ",
		   "  WHERE	hrs_dept.org_id=#{org_id}	and hrs_dept.dept_code like concat(#{dept_code},'___') ",
		    "<when test='dept_status!=null'>",
		    " AND hrs_dept.dept_status = #{dept_status}",
		    "</when>",
		    "<when test='dept_code!=null and dept_code!=\"\"'>",
		    " AND hrs_dept.dept_code like  \"%\"#{dept_code}\"%\"",
		    "</when>",
		    "<when test='dept_name!=null and dept_name!=\"\"'>",            
		    " AND hrs_dept.dept_name like  \"%\"#{dept_name}\"%\"",
		    "</when>",
		  "</script>"})
//	@Select("SELECT hrs_dept.*,hrs_user.user_name managerName from 	hrs_dept\n"
//			+" left join hrs_user on hrs_dept.manager_id=hrs_user.user_id \n"
//			+ "	 WHERE	hrs_dept.org_id=#{org_id}	and hrs_dept.dept_code like concat(#{dept_code},'___')\n")
	List<Dept> getDeptListChildren(@Param("org_id") Integer org_id,@Param("dept_status") Boolean dept_status,@Param("dept_code") String dept_code,@Param("dept_name") String dept_name);
	
	@Select({"<script>",
			"select user_id value,user_name label ,job_number label2,CONCAT(user_name ,' ',IFNULL(user_email,'')) label3,user_email  label4 from hrs_user\n",
	 "		where user_status=1 and isadmin !=1 AND org_id = #{org_id} \n",
//	 "      <when test='dept_id!=null and dept_id!=\"\"'>",
//	    "   AND dept_id = #{dept_id}",
//	    "   </when>",
	 "		order by user_id\n",
	"</script>"})
	List<Map<String, Object>> selectUser(@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id);
	@Select({"<script>",
		"SELECT a.user_id value,a.user_name label ,a.job_number label2 FROM hrs_user a\n",
//		"LEFT JOIN hrs_dept b ON a.dept_id=b.dept_id \n",
		"	 where a.user_status=1 and a.isadmin !=1 AND a.leave_date is null AND a.blacklist_date is null AND a.org_id = #{org_id}\n",
//		"      <when test='dept_code!=null and dept_code!=\"\"'>",
//		"   AND b.dept_code = #{dept_code}",
//		"   </when>",
		"		order by a.user_id\n",
	"</script>"})
	List<Map<String, Object>> selectUserByCode(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT a.user_id FROM hrs_user a\n"
	+ "		LEFT JOIN hrs_dept b \n"
	+ "		ON a.dept_id=b.dept_id\n"
	+ "	 WHERE	a.org_id=#{org_id} and a.user_status=1	and b.dept_code like concat(#{dept_code},'%')\n")
	List<User> selectUserByDeptId(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.dept_code like concat(#{dept_code},'___') ")
	List<Dept> selectDeptByDeptCode(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.is_final_profitunit=1 and hrs_dept.dept_code like concat(#{dept_code},'_%') ")
	List<Dept> getlCList(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.is_final_profitunit=1 and hrs_dept.dept_code like concat(#{dept_code},'%') ")
	List<Dept> getlCList2(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.is_final_profitunit=1 and hrs_dept.dept_code =#{dept_code}")
	List<Dept> getlList(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE org_id=#{org_id} and dept_name=#{dept_name} and dept_code like concat(#{dept_code},'___') ")
	List<Dept> checkDeptName(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("dept_name") String dept_name);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE org_id=#{org_id} and short_name=#{short_name} and dept_code like concat(#{dept_code},'___') ")
	List<Dept> checkDeptShortName(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("short_name") String short_name);
	@Select("SELECT hrs_dept.* FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.is_final_profitunit=1 "
			+ "and hrs_dept.dept_code !=#{dept_code2} and hrs_dept.dept_code like concat(#{dept_code},'_%') ")
	List<Dept> getListByDept(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("dept_code2") String dept_code2);
	
	@Update("update hrs_dept \n"
			+ " set dept_status=0 \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_code like concat(#{dept_code},'%')\n")
	void stopById(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Delete("delete from hrs_dept \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_code like concat(#{dept_code},'%')\n")
	void deleteById2(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	
	@Update("update hrs_dept \n"
			+ " set full_name=REPLACE(full_name,#{oldDeptName},#{dept_name}) \n"
			+ "	 WHERE	org_id=#{org_id} and dept_code like concat(#{dept_code},'%')\n")
	void updateChild(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("oldDeptName") String oldDeptName,@Param("dept_name") String dept_name);
	@Update("update hrs_dept \n"
			+ " set dept_status=1 \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_code like concat(#{dept_code},'%')\n")
	void startById(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Update("update hrs_dept \n"
			+ " set is_profitunit=#{is_profitunit} \n"
			+ "	 WHERE	org_id=#{org_id} and dept_code =#{dept_code}\n")
	void updateIsProfitunitByDeptCode(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("is_profitunit") Integer is_profitunit);
	
	@Update("update hrs_user \n"
			+ " set dept_id=#{dept_id} \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_id =#{deptIdSelect}\n")
	void updateUserOfDept(@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id,@Param("deptIdSelect") Integer deptIdSelect);
	@Update("update hrs_dept \n"
			+ " set dept_status=0 \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_id =#{dept_id}\n")
	void updateDeptStatus(@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id);
	@Delete("delete from hrs_dept \n"
			+ "	 WHERE	org_id=#{org_id}	and dept_id =#{dept_id}\n")
	void deleteDeptStatus(@Param("org_id") Integer org_id,@Param("dept_id") Integer dept_id);
	@Update("update hrs_dept \n"
//			+ " set dept_code=CONCAT(#{dept_code},SUBSTRING(dept_code,4)) \n"
				+ " set dept_code=CONCAT(#{dept_code},SUBSTRING(dept_code,#{length})) \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_code like concat(#{dept_code2},'%') ")
	void moveDept(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("dept_code2") String dept_code2,@Param("length") Integer length);
	@Update("update hrs_dept \n"
			+ " set dept_code=CONCAT(#{dept_code},SUBSTRING(dept_code,#{length})) \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.dept_code like concat(#{dept_code2},'%') ")
	void moveDept2(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code,@Param("dept_code2") String dept_code2,@Param("length") Integer length);
	@Delete("delete FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=0 and hrs_dept.dept_code like concat(#{dept_code},'___') ")
	void deleteChild(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT MAX(dept_code) FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_code like concat(#{dept_code},'___') ")
	String getMaxDeptCode(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	@Select("SELECT MAX(dept_code) FROM hrs_dept \n"
			+ "	 WHERE	hrs_dept.org_id=#{org_id} and hrs_dept.dept_status=1 and hrs_dept.dept_code like concat(#{dept_code},'%') ")
	String getAllMaxDeptCode(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);
	
	@Select({"<script>",
		   "SELECT hrs_dept.*,hrs_user.user_name managerName,c.actualHc from 	hrs_dept ",
		   " left join hrs_user on hrs_dept.manager_id=hrs_user.user_id ",
		   " LEFT JOIN  ",
		   " (SELECT dept_id,COUNT(1) actualHc  FROM hrs_user WHERE user_status=1 and org_id=#{org_id}  AND isadmin!=1",
		   " GROUP BY dept_id) c ",
		   "  ON hrs_dept.dept_id=c.dept_id ",
		   "  WHERE	hrs_dept.org_id=#{org_id} ",
		    "<when test='dept_status!=null'>",
		    " AND hrs_dept.dept_status = #{dept_status}",
		    "</when>",
		    "<when test='dept_name!=null and dept_name!=\"\"'>",            
		    " AND hrs_dept.dept_name like  \"%\"#{dept_name}\"%\"",
		    "</when>",
		    " ORDER BY hrs_dept.dept_code asc ",
		  "</script>"})
	List<Dept> getDeptbyOrgid(@Param("org_id") Integer org_id,@Param("dept_name") String dept_name,@Param("dept_status") Boolean dept_status);
	
	@Select({"<script>",
		   "SELECT a.dept_name deptName,a.dept_code deptCode,a.short_name shortName,a.full_name fullName, a.budget_hc budgetHc,",
		" CASE WHEN a.is_profitunit=1 THEN '是' WHEN a.is_profitunit=0 THEN '否' ELSE '' END isProfitunit, ",
		" CASE WHEN a.is_final_profitunit=1 THEN '是' WHEN a.is_final_profitunit=0 THEN '否' ELSE '' END isFinalProfitunit, ",
		" CASE WHEN a.dept_status=1 THEN '生效' ELSE '失效' END deptStatus,",
		" CASE WHEN hrs_user.user_name IS NULL THEN '' ELSE  hrs_user.user_name END managerName, ",
		" CASE WHEN c.actualHc IS NULL THEN '' ELSE c.actualHc END actualHc ",
		" FROM hrs_dept a",
		   " left join hrs_user on a.manager_id=hrs_user.user_id ",
		   " LEFT JOIN  ",
		   " (SELECT dept_id,COUNT(1) actualHc  FROM hrs_user WHERE user_status=1 and org_id=#{orgId}",
		   " GROUP BY dept_id) c ",
		   "  ON a.dept_id=c.dept_id ",
            " where " +
            " a.org_id = #{orgId}" +
            "<when test='deptStatus!=null'>" +
            "    and a.dept_status = #{deptStatus}" +
            "</when>" +
            " order by a.dept_code" +
            "</script>"})
    List<DeptExcel> queryListForExcel(Dept bean);
	@Select({"<script>",
		"SELECT a.user_id  "
		+ " FROM hrs_user a "
		+ " LEFT JOIN hrs_dept  b ON a.dept_id=b.dept_id "
		+ " WHERE a.org_id=#{org_id} AND a.isadmin!=1 AND a.user_status=1 AND b.dept_code LIKE CONCAT(#{dept_code},'%')" +
	"</script>"})
	List<User> queryUserList(@Param("org_id") Integer org_id,@Param("dept_code") String dept_code);

	@Select({"<script>",
			"SELECT\n" +
					"\tu.user_email \n" +
					"FROM\n" +
					"\thrs_user_mail_cc cc\n" +
					"\tINNER JOIN hrs_user u ON cc.user_id_cc = u.user_id and cc.org_id = u.org_id \n" +
					"WHERE\n" +
					"\tcc.org_id = #{bean.orgId} \n" +
					"\tAND cc.user_id = #{bean.userId} \n" +
					"\tAND cc.permission_name = #{bean.permissionName}" +
					"\tAND u.user_status = 1 \n" +
					"</script>"})
	List<String> selectOrderTrackCcUser(@Param("bean") UserMailCc userMailCc);

	@Select({"<script>",
			"SELECT\n" +
					"\tcc.user_id_cc \n" +
					"FROM\n" +
					"\thrs_user_mail_cc cc\n" +
					"\tINNER JOIN hrs_user u ON cc.user_id_cc = u.user_id and cc.org_id = u.org_id \n" +
					"WHERE\n" +
					"\tcc.org_id = #{bean.orgId} \n" +
					"\tAND cc.user_id = #{bean.userId} \n" +
					"\tAND cc.permission_name = #{bean.permissionName}" +
					"\tAND u.user_status = 1 \n" +
					"</script>"})
	List<Integer> selectOrderTrackCcUserId(@Param("bean") UserMailCc userMailCc);
}

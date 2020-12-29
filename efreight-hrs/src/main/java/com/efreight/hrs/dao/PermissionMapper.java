package com.efreight.hrs.dao;

import com.efreight.hrs.entity.Permission;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface PermissionMapper extends BaseMapper<Permission> {
    Permission selectByPermissionId(Integer permissionId);

    //	@Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_permission"
//			+ "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
//			+ "		WHERE" + "			hrs_permission.`status` = 1"
//			+ "		AND hrs_role_permission.role_id = #{roleId}" + "		ORDER BY"
//			+ "			hrs_permission.sort DESC")
    @Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_permission"
            + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"
            + "		LEFT JOIN hrs_org_permission on hrs_permission.`permission_id`=hrs_org_permission.`permission_id`"
            + "		WHERE" + "			hrs_permission.`status` = 1"
            + "		AND hrs_permission.`disabled`=\"false\"  "
           /* + "		AND hrs_permission.`admin_default`=\"false\"  "*/
            + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id = #{roleId}"
            + "		AND hrs_org_permission.org_id= #{orgId} "
            + "		AND hrs_org_permission.permission_status=1  "
            + "		ORDER BY"
            + "			hrs_permission.sort DESC")
    List<Permission> selectByPermissionRoleId(@Param("roleId") Integer roleId,@Param("orgId") Integer orgId);

    @Select({"<script>",
            "SELECT\n" +
                    "\tA.permission_id,\n" +
                    "\tA.permission_code,\n" +
                    "\tA.permission_name,\n" +
                    "\tA.url,\n" +
                    "\tA.permission,\n" +
                    "\tA.icon,\n" +
                    "\tA.parent_id,\n" +
                    "\tA.parent_ids,\n" +
                    "\tA.sort,\n" +
                    "\tA.STATUS,\n" +
                    "\tA.permission_type,\n" +
                    "\tA.creator_id,\n" +
                    "\tA.create_time,\n" +
                    "\tA.stop_date,\n" +
                    "\tA.app_code,\n" +
                    "\tA.disabled,\n" +
                    "\tA.admin_default,\n" +
                    "\tA.able_Orgid,\n" +
                    "\tA.help_document_name,\n" +
                    "\tA.help_document_url,\n" +
                    "CASE\n" +
                    "\t\t\n" +
                    "\t\tWHEN C.permission_id IS NULL THEN\n" +
                    "\t\t'/unPermission' \n" +
                    "\t\tWHEN C1.permission_id IS NULL THEN\n" +
                    "\t\t'/unPermission' ELSE A.path \n" +
                    "\tEND AS path " +
            "FROM hrs_permission A\n" +
            "LEFT JOIN (\n" +
            " SELECT permission_id FROM hrs_role_permission WHERE role_id = 1 AND  permission_id !=1 AND permission_id !=46 \n" +
            ") AS B ON A.permission_id=B.permission_id \n" +
            "LEFT JOIN hrs_org_permission C ON A.permission_id=C.permission_id AND C.permission_status=1 AND C.org_id=#{orgId} \n" +
            "LEFT JOIN hrs_role_permission C1 ON C.permission_id=C1.permission_id AND C1.org_id=C.org_id AND C1.role_id=#{roleId}\n" +
            "WHERE 1=1\n" +
            " AND A.status = 1\n" +
            " AND A.disabled = 'false'\n" +
            " AND B.permission_id IS NULL" +
            "<when test='orgId != \"1\"'>"+
            " AND A.permission_id != 194 "+
            "</when>"+
            "</script>"})
    List<Permission> selectByAllPermissionRoleId(@Param("roleId") Integer roleId,@Param("orgId") Integer orgId);

    @Select({"<script>",
            "SELECT \n" +
                    " A.permission_id,\n" +
                    " A.permission_name AS permission_name,\n" +
                    " A.permission_code,\n" +
                    " A.url,\n" +
                    " A.permission,\n" +
                    " A.icon,\n" +
                    " A.parent_id,\n" +
                    " A.parent_ids,\n" +
                    " A.sort,\n" +
                    " A.STATUS,\n" +
                    " A.permission_type,\n" +
                    " A.creator_id,\n" +
                    " A.create_time,\n" +
                    " A.stop_date,\n" +
                    " A.app_code,\n" +
                    " A.disabled,\n" +
                    " A.admin_default,\n" +
                    " A.able_Orgid,\n" +
                    " A.help_document_name,\n" +
                    " A.help_document_url,\n" +
                    " CASE WHEN C1.permission_id IS NULL OR C.permission_id IS NULL \n" +
                    " THEN '/unPermission' \n" +
                    " ELSE A.path END AS path " +
                    "FROM hrs_permission A\n" +
                    "LEFT JOIN ( \n" +
                    " SELECT permission_id \n" +
                    " FROM hrs_role_permission \n" +
                    " WHERE org_id=1  \n" +
                    "  AND role_id = 1  \n" +
                    "  AND permission_id NOT IN (1,46) GROUP BY  permission_id \n" +
                    ") AS B ON A.permission_id = B.permission_id\n" +
                    "LEFT JOIN (\n" +
                    " SELECT permission_id FROM hrs_org_permission\n" +
                    " WHERE org_id=#{orgId} \n" +
                    "  AND permission_status=1 GROUP BY  permission_id \n" +
                    ") C ON A.permission_id = C.permission_id \n" +
                    "LEFT JOIN (\n" +
                    " SELECT permission_id FROM hrs_role_permission\n" +
                    " WHERE org_id=#{orgId} \n" +
                    "  AND role_id IN (${roles}) GROUP BY  permission_id \n" +
                    ") C1 ON C.permission_id = C1.permission_id \n" +
                    "WHERE A.STATUS = 1 \n" +
                    " AND A.disabled = 'false' \n" +
                    " AND B.permission_id IS NULL \n" +
                    " AND A.permission_type IN (0,1) \n" +
                    "<when test='orgId != \"1\"'>"+
                    " AND A.permission_id != 194 "+
                    "</when>"+
                    "GROUP BY A.permission_id" +
                    "</script>"})
    List<Permission> selectByAllPermissionRoleId1(@Param("roles") String roles,@Param("orgId") Integer orgId);

    @Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_permission"
            + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"
            + "		WHERE" + "			hrs_permission.`status` = 1"
            + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id = #{roleId}"
            + "		ORDER BY"
            + "			hrs_permission.sort DESC")
    List<Permission> selectByPermissionRoleIdForAdmin(@Param("roleId") Integer roleId);

    @Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_permission"
           /* + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"*/
            + "		WHERE" + "			hrs_permission.`status` = 1"
            + "		AND hrs_permission.`disabled`=\"false\"  "
            + "		AND hrs_permission.`admin_default`=\"true\"  "
           /* + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id = #{roleId}"*/
            + "		ORDER BY"
            + "			hrs_permission.sort DESC")
    List<Permission> selectByPermissionRoleIdForAdmin1(@Param("roleId") Integer roleId);

    @Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_permission"
            + "		LEFT JOIN hrs_org_permission ON hrs_permission.permission_id = hrs_org_permission.permission_id"
            + "		WHERE" + "			hrs_permission.`status` = 1"
            + "		AND hrs_org_permission.org_id = #{orgId}" + "		ORDER BY"
            + "			hrs_permission.sort DESC")
    List<Permission> selectByPermissionOrgId(@Param("orgId") Integer roleId);

    @Select("SELECT" + "			hrs_permission.*" + "		FROM" + "			hrs_org_permission "
            + "		LEFT JOIN hrs_permission ON hrs_org_permission.permission_id = hrs_permission.permission_id"
            + "		WHERE" + "			hrs_permission.`status` = 1"
            + "		AND (  hrs_permission.disabled = 'false'  or hrs_permission.able_orgid in( #{orgId} )  ) "
           /* + "		AND	hrs_permission.`admin_default` =\"false\""*/
            + "		AND hrs_org_permission.org_id = #{orgId}" + "		ORDER BY"
            + "			hrs_permission.sort ASC")
    List<Permission> getRoleTree(@Param("orgId") Integer roleId);
    @Select("SELECT DISTINCT hrs_permission.permission FROM hrs_permission"
            + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"
            + "		LEFT JOIN hrs_org_permission on hrs_permission.`permission_id`=hrs_org_permission.`permission_id`"
            + "		WHERE hrs_permission.`status` = 1 and permission_type=2"
            + "		AND hrs_permission.`disabled`=\"false\"  "
           /* + "		AND hrs_permission.`admin_default`=\"false\"  "*/
            + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id in (${roles})"
            + "		AND hrs_org_permission.org_id= #{orgId} "
            + "		AND hrs_org_permission.permission_status=1  "
            )
    List<String> getButtonInfo(@Param("roles") String roles,@Param("orgId") Integer orgId);

    @Select("SELECT DISTINCT hrs_permission.permission FROM hrs_permission"
            + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"
            + "		WHERE hrs_permission.`status` = 1 and permission_type=2"
            + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id in (${roles})"
    )
    List<String> getButtonInfoForAdmin(@Param("roles") String roles);

    @Select("SELECT DISTINCT hrs_permission.permission FROM hrs_permission"
            + "		LEFT JOIN hrs_role_permission ON hrs_permission.permission_id = hrs_role_permission.permission_id"
            + "		LEFT JOIN hrs_role ON hrs_role_permission.`role_id`=hrs_role.`role_id`"
            + "		WHERE hrs_permission.`status` = 1 and permission_type=2"
            + "		AND hrs_permission.`disabled`=\"false\"  "
            + "		AND hrs_permission.`admin_default`=\"true\"  "
            + "		AND hrs_role.`role_status`=1  "
            + "		AND hrs_role_permission.role_id in (${roles})"
    )
    List<String> getButtonInfoForAdmin1(@Param("roles") String roles);


    @Select("SELECT p.permission_id,p.permission_name,p.parent_id,p.sort FROM hrs_permission p"
            + "		LEFT JOIN hrs_role_permission rp ON p.permission_id = rp.permission_id"
            + "		LEFT JOIN hrs_role r ON rp.role_id=r.role_id"
            + "		WHERE p.status = 1 and p.permission_type in (0,1)"
            + "		AND r.role_status=1 and r.role_id=#{roleId} and r.org_id=#{orgId} order by p.sort")
    List<Permission> getPermissionTreeForHomePage(@Param("orgId") Integer orgId,@Param("roleId") Integer roleId);

    @Select("SELECT" + "	hrs_permission.*" + "	FROM" + "	hrs_permission where admin_default='true' ")
    List<Permission> selectAdPermission();

    @Select("SELECT" + "	hrs_role.isadmin AS isAdmin" + "	FROM" + "	hrs_role where hrs_role.role_id=#{roleId} ")
    Integer getRoleByRoleId(@Param("roleId") Integer roleId);
}

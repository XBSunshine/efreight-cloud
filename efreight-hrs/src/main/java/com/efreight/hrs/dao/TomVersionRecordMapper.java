package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.hrs.entity.TomVersionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author xiaobo
 * @since 2019-07-22
 */
public interface TomVersionRecordMapper extends BaseMapper<TomVersionRecord> {

    @Select({"<script>select a.*,b.permission_name permissionName from tom_version_record a" +
            " left join hrs_permission b on b.permission_id=a.permission_id" +
            " where 1=1 and a.version_id=#{versionId}" +
            " <when test='permissionId != null'>" +
            " and a.permission_id=#{permissionId}" +
            " </when>" +
            "<when test='updateType !=null and updateType != \"\"'>" +
            " and a.update_type = #{updateType}" +
            "</when>" +
            " order by a.record_id desc</script>"})
    IPage<TomVersionRecord> selectPageSelf(Page page, @Param("versionId") Integer versionId, @Param("permissionId") Integer permissionId, @Param("updateType") String updateType);
}

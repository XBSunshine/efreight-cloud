package com.efreight.hrs.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.efreight.hrs.entity.Log;
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
public interface LogMapper extends BaseMapper<Log> {

	@Select({"<script>",
		   "select a.log_id,a.op_level,a.op_type,a.op_name,a.op_info,a.creator_id,a.create_time,a.org_id,",
		   " a.dept_id, b.user_name as creator_name from hrs_log a ",
		   " left join hrs_user b on a.creator_id=b.user_id ",
		   " WHERE 1=1 ",
		    " AND a.org_id = #{bean.orgId}",
		    "<when test='bean.opLevel!=null and bean.opLevel!=\"\"'>",
		    " AND a.op_level like  \"%\"#{bean.opLevel}\"%\"",
		    "</when>",
		    "<when test='bean.opName!=null and bean.opName!=\"\"'>",
		    " AND a.op_name like  \"%\"#{bean.opName}\"%\"",
		    "</when>",
		    "<when test='bean.creatorName!=null and bean.creatorName!=\"\"'>",
		    " AND b.user_name like  \"%\"#{bean.creatorName}\"%\"",
		    "</when>",
		    "<when test='bean.createTimeBegin!=null and bean.createTimeBegin!=\"\"'>",            
		    " AND a.create_time &gt;= #{bean.createTimeBegin}",
		    "</when>",
		    "<when test='bean.createTimeEnd!=null and bean.createTimeEnd!=\"\"'>",            
		    " AND a.create_time &lt;= #{bean.createTimeEnd}",
		    "</when>",
		    "order by log_id desc",
		  "</script>"})
	IPage<Log> getLogList(Page page,@Param("bean") Log bean);
}

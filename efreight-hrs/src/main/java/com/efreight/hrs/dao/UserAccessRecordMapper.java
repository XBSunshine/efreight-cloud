package com.efreight.hrs.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.UserAccessRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author lc
 * @date 2020/5/12 14:56
 */
public interface UserAccessRecordMapper extends BaseMapper<UserAccessRecord> {

    /**
     * 记录数据自增1
     * @param path
     * @return
     */
    @Update("UPDATE hrs_user_access_record SET records_number = records_number + 1, edit_time = NOW() WHERE user_id=#{userId} and path=#{path}")
    int incrementRecord(@Param("userId")Integer userId, @Param("path") String path);

    @Select("SET @rank_number = 0;\n" +
            "SELECT\n" +
            "\taa.*,\n" +
            "\t@rank_number := IFNULL( @rank_number, 0 )+ 1 AS rankNumber \n" +
            "FROM\n" +
            "\t(\n" +
            "\tSELECT\n" +
            "\t\tA.* \n" +
            "\tFROM\n" +
            "\t\thrs_org A\n" +
            "\t\tINNER JOIN hrs_org B ON A.org_edition_id = B.org_id \n" +
            "\tWHERE\n" +
            "\t\tA.org_type = 1 \n" +
            "\t\tAND B.org_name NOT LIKE '%内部%' \n" +
            "\tORDER BY\n" +
            "\t\tA.active_index DESC \n" +
            "\tLIMIT 30 \n" +
            "\t) aa ")
    List<Org> selectTopActiveIndex();

}

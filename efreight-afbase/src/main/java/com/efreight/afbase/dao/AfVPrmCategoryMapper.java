package com.efreight.afbase.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.efreight.afbase.entity.AfVPrmCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 参数表 Mapper 接口
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
public interface AfVPrmCategoryMapper extends BaseMapper<AfVPrmCategory> {

    @Select({"<script>",
            "SELECT param_text, param_ranking, category_name, remarks, EDICode1  FROM af_V_prm_category ",
            "	where category_name=#{categoryName} AND EDICode1!=''",
            "</script>"})
    List<AfVPrmCategory> getAfVPrmCategory(@Param("categoryName") String categoryName);

    @Select({"<script>",
            "SELECT param_text, param_ranking, category_name, remarks, EDICode1  FROM af_V_prm_category ",
            "	where category_type=#{categoryType}",
            "</script>"})
    List<AfVPrmCategory> queryCategoryByCategoryType(@Param("categoryType") Integer categoryType);


    @Select("<script> SELECT param_text FROM af_V_prm_category WHERE category_type = 7 AND param_text != 'EF' </script>")
    List<AfVPrmCategory> findDocBusinessScope();
}

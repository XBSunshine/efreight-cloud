package com.efreight.prm.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.prm.entity.BillServiceGroup;
import com.efreight.prm.entity.Coop;
import com.efreight.prm.entity.CoopDetail;
import com.efreight.prm.entity.CoopExcel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface CoopDao {

    //查询列表
    List<Coop> queryCoopList(Map<String, Object> paramMap);

    List<Coop> queryCoopListByPage(Map<String, Object> paramMap);

    //查询子列表
    List<Coop> queryCoopChildList(Map<String, Object> paramMap);
    List<Coop> queryCoopChildList1(Map<String, Object> paramMap);
    List<Coop> queryCoopChildList2(Map<String, Object> paramMap);
    List<Coop> queryCoopChildList3(Map<String, Object> paramMap);
    List<Coop> queryCoopChildList4(Map<String, Object> paramMap);
    
    //查询列表
    List<Coop> queryTreeList(Coop coop);
    
    //插入
    Integer saveCoop(Coop coop);

    //修改
    Integer modifyCoop(Coop coop);

    Coop viewCoop(Map paramMap);

    //为其他功能选择客商资料提供的方法(新建页面使用)
    List<Map<String, Object>> queryListForChoose(Map<String, Object> paramMap);

    //为其他功能选择客商资料提供的方法（修改页面使用）
    List<Map<String, Object>> queryListForChooseForUpdate(Map<String, Object> paramMap);

    //修改客商资料黑白名单含子客商资料
    void modifyBlackWhiteValidIncludeChildren(@Param("coopCode") String coopCode, @Param("blackWhiteValid") String blackWhiteValid, @Param("blackWhiteReason") String blackWhiteReason, @Param("orgId") Integer orgId, @Param("currentTime") LocalDateTime currentTime);

    void modifyOutBlackWhiteValidIncludeChildren(@Param("coopCode") String coopCode, @Param("blackWhiteValid") String blackWhiteValid, @Param("blackWhiteReason") String blackWhiteReason, @Param("orgId") Integer orgId, @Param("currentTime") LocalDateTime currentTime);

    //解锁、开锁
    void lock(@Param("coopCode") String coopCode, @Param("lockReason") String lockReason, @Param("now") LocalDateTime now,@Param("orgId") String orgId);

    void unLock(@Param("coopId") String coopId, @Param("unlockReason") String unlockReason);

    //导出excel

    List<CoopExcel> queryListForExcel(Map<String, Object> paramMap);
    
    void modifyCoopCode(@Param("oriCoopCode") String oriCoopCode, @Param("afCoopCode") String afCoopCode, @Param("orgId") String orgId);
    
    List<Coop> queryCoopCodes(@Param("coopCode") String coopCode,@Param("orgId") String orgId);
    Coop queryCoopCodeByCoop(@Param("coopCode") String coopCode,@Param("orgId") String orgId);

    List<Coop> queryAllChildrenByCode(@Param("coopCode")String coopCode, @Param("orgId")Integer orgId);

    List<Coop> queryCoopList1(Map<String, Object> paramMap);

    List<CoopDetail> queryCoopChildList11(Map<String, Object> paramMap);

    Coop queryCoopCodeByCoopName(@Param("coopName") String coopName,@Param("orgId") String orgId);

    Coop queryCoopCodeByCoopName1(@Param("coopName") String coopName,@Param("orgId") String orgId,@Param("coopId") Integer coopId);

    Coop queryCoopCodeByShortName(@Param("shortName") String shortName,@Param("orgId") String orgId);

    Coop queryCoopCodeByShortName1(@Param("shortName") String shortName,@Param("orgId") String orgId,@Param("coopId") Integer coopId);

    Coop queryCoopCodeByCoopEName(@Param("coopEName") String coopEName,@Param("orgId") String orgId);

    Coop queryCoopCodeByCoopEName1(@Param("coopEName") String coopEName,@Param("orgId") String orgId,@Param("coopId") Integer coopId);

    Coop queryCoopCodeByShortEName(@Param("shortEName") String shortEName,@Param("orgId") String orgId);

    Coop queryCoopCodeByShortEName1(@Param("shortEName") String shortEName,@Param("orgId") String orgId,@Param("coopId") Integer coopId);

    Coop queryCoopCodeBySocialCreditCode(@Param("socialCreditCode") String shortEName,@Param("orgId") String orgId);

    Integer isHaveSocialCreditCode(Coop coop);

    List<Coop> listByType(Map<String, Object> paramMap);

    List<String> getBusinessScope();

    List<Coop> listByCoopName(Map<String, Object> paramMap);

    Integer getUserCountByUserId(Integer userId);

    @Select("call prm_P_CoopsForAwb(#{orgId},#{businessScope})")
    List<Coop> selectPrmCoopsForAwb(@Param("orgId") Integer orgId,@Param("businessScope") String businessScope);
}

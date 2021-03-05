package com.efreight.prm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopAddressDao;
import com.efreight.prm.dao.CoopContactsDao;
import com.efreight.prm.dao.CoopDao;
import com.efreight.prm.dao.CoopScopeDao;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopLockService;
import com.efreight.prm.service.CoopService;
import com.efreight.prm.service.LogService;
import com.efreight.prm.util.PDFUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class CoopServiceImpl implements CoopService {

    private final CoopDao coopDao;
    private final LogService logService;
    private final CoopLockService coopLockService;
    private CoopScopeDao coopScopeDao;
    private CoopAddressDao coopAddressDao;
    private CoopContactsDao coopContactsDao;

    @Override
    public Map<String, Object> queryCoopList(Integer currentPage, Integer pageSize, Map paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<Coop> persons = coopDao.queryCoopList(paramMap);
        System.out.println("========" + persons);
        Map<String, Object> paramMap1 = new HashMap<String, Object>();
        Map<String, Object> paramMap2 = new HashMap<String, Object>();
        Map<String, Object> paramMap3 = new HashMap<String, Object>();
        Map<String, Object> paramMap4 = new HashMap<String, Object>();
        paramMap1.put("coop_mnemonic", paramMap.get("coop_mnemonic"));
        paramMap2.put("coop_mnemonic", paramMap.get("coop_mnemonic"));
        paramMap3.put("coop_mnemonic", paramMap.get("coop_mnemonic"));
        paramMap4.put("coop_mnemonic", paramMap.get("coop_mnemonic"));

        paramMap1.put("social_credit_code", paramMap.get("social_credit_code"));
        paramMap2.put("social_credit_code", paramMap.get("social_credit_code"));
        paramMap3.put("social_credit_code", paramMap.get("social_credit_code"));
        paramMap4.put("social_credit_code", paramMap.get("social_credit_code"));

        paramMap1.put("coop_name", paramMap.get("coop_name"));
        paramMap2.put("coop_name", paramMap.get("coop_name"));
        paramMap3.put("coop_name", paramMap.get("coop_name"));
        paramMap4.put("coop_name", paramMap.get("coop_name"));

        paramMap1.put("coop_status", paramMap.get("coop_status"));
        paramMap2.put("coop_status", paramMap.get("coop_status"));
        paramMap3.put("coop_status", paramMap.get("coop_status"));
        paramMap4.put("coop_status", paramMap.get("coop_status"));

        paramMap1.put("black_valid", paramMap.get("black_valid"));
        paramMap2.put("black_valid", paramMap.get("black_valid"));
        paramMap3.put("black_valid", paramMap.get("black_valid"));
        paramMap4.put("black_valid", paramMap.get("black_valid"));

        paramMap1.put("white_valid", paramMap.get("white_valid"));
        paramMap2.put("white_valid", paramMap.get("white_valid"));
        paramMap3.put("white_valid", paramMap.get("white_valid"));
        paramMap4.put("white_valid", paramMap.get("white_valid"));

        paramMap1.put("credit_level", paramMap.get("credit_level"));
        paramMap2.put("credit_level", paramMap.get("credit_level"));
        paramMap3.put("credit_level", paramMap.get("credit_level"));
        paramMap4.put("credit_level", paramMap.get("credit_level"));

        paramMap1.put("coop_code", paramMap.get("coop_code"));
        paramMap2.put("coop_code", paramMap.get("coop_code"));
        paramMap3.put("coop_code", paramMap.get("coop_code"));
        paramMap4.put("coop_code", paramMap.get("coop_code"));
        if (persons != null && persons.size() > 0) {
            for (int i = 0; i < persons.size(); i++) {
                persons.get(i).setId(UUID.randomUUID().toString());
                if (persons.get(i).getGroup_type() == "group" || "group".equals(persons.get(i).getGroup_type())) {//说明还有子集
                    paramMap1.put("coop_code", persons.get(i).getCoop_code());
                    List<Coop> coopList1 = coopDao.queryCoopChildList1(paramMap1);//加载第一级子集
                    if (coopList1 != null && coopList1.size() > 0) {
                        for (int j = 0; j < coopList1.size(); j++) {
                            coopList1.get(j).setId(UUID.randomUUID().toString());
                            if (coopList1.get(j).getGroup_type() == "group" || "group".equals(coopList1.get(j).getGroup_type())) {
                                paramMap2.put("coop_code", coopList1.get(j).getCoop_code());
                                List<Coop> coopList2 = coopDao.queryCoopChildList2(paramMap2);//加载第二级子集
                                if (coopList2 != null && coopList2.size() > 0) {
                                    for (int k = 0; k < coopList2.size(); k++) {
                                        coopList2.get(k).setId(UUID.randomUUID().toString());
                                        if (coopList2.get(k).getGroup_type() == "group" || "group".equals(coopList2.get(k).getGroup_type())) {
                                            paramMap3.put("coop_code", coopList2.get(k).getCoop_code());
                                            List<Coop> coopList3 = coopDao.queryCoopChildList3(paramMap3);//加载第三级子集
                                            if (coopList3 != null && coopList3.size() > 0) {
                                                for (int n = 0; n < coopList3.size(); n++) {
                                                    coopList3.get(n).setId(UUID.randomUUID().toString());
                                                    if (coopList3.get(n).getGroup_type() == "group" || "group".equals(coopList3.get(n).getGroup_type())) {
                                                        paramMap4.put("coop_code", coopList3.get(n).getCoop_code());
                                                        List<Coop> coopList4 = coopDao.queryCoopChildList4(paramMap4);//加载第四级子集
                                                        if (coopList4 != null && coopList4.size() > 0) {
                                                            for (int p = 0; p < coopList4.size(); p++) {
                                                                coopList4.get(p).setId(UUID.randomUUID().toString());
                                                            }
                                                        }
                                                        coopList3.get(n).setChildren(coopList4);
                                                    }
                                                }
                                            }
                                            coopList2.get(k).setChildren(coopList3);
                                        }
                                    }
                                }
                                coopList1.get(j).setChildren(coopList2);
                            }
                        }
                    }
                    persons.get(i).setChildren(coopList1);
                }
            }
        }
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public Map<String, Object> queryCoopListByPage(Integer currentPage, Integer pageSize, Map paramMap) {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        if (currentPage == null || currentPage == 0)
            currentPage = 1;
        if (pageSize == null || pageSize == 0)
            pageSize = 10;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<Coop> persons = coopDao.queryCoopListByPage(paramMap);
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public List<Coop> queryCoopList1(Map paramMap) {
        List<Coop> flightPage = coopDao.queryCoopList1(paramMap);
        Map<String, Object> paramMap1 = new HashMap<String, Object>();
        Map<String, Object> paramMap2 = new HashMap<String, Object>();
        if (flightPage != null && flightPage.size() > 0) {
            //List<Coop> flightList = flightPage.getRecords();
            for (int i = 0; i < flightPage.size(); i++) {
                Coop flight = flightPage.get(i);
                paramMap1.put("coop_code", flightPage.get(i).getCoop_code());
                flight.setId(UUID.randomUUID().toString());
                List<Coop> fds = coopDao.queryCoopChildList(paramMap1);
                if (fds != null && fds.size() > 0) {
                    for (int j = 0; j < fds.size(); j++) {
                        paramMap2.put("coop_code", fds.get(j).getCoop_code());
                        //根据客商资料代码查询其是否有子公司
                        List<Coop> childList = coopDao.queryCoopChildList(paramMap2);
                        if (childList != null && childList.size() > 0) {
                            fds.get(j).setHasChildren(true);
                        } else {
                            fds.get(j).setHasChildren(false);
                        }
                        fds.get(j).setId(UUID.randomUUID().toString());
                    }
                }
                flightPage.get(i).setChildren(fds);
            }
            //flightPage.setRecords(flightList);
        }
        return flightPage;
    }

    @Override
    public List<Coop> getTreeList(Coop coop) {
        List<Coop> list = coopDao.queryTreeList(coop);
        return list;
    }

    @Override
    public Map<String, Object> queryCoopChildList(Integer currentPage, Integer pageSize, Map paramMap) {
//		if(currentPage==null||currentPage==0)
        currentPage = 1;
//    	if(pageSize==null||pageSize==0)
        pageSize = 100;
        Page Page = PageHelper.startPage(currentPage, pageSize, true);
        List<Coop> persons = coopDao.queryCoopChildList(paramMap);
        Map<String, Object> paramMap1 = new HashMap<String, Object>();
        if (persons != null && persons.size() > 0) {
            for (int i = 0; i < persons.size(); i++) {
                paramMap1.put("coop_code", persons.get(i).getCoop_code());
                //根据客商资料代码查询其是否有子公司
                List<Coop> childList = coopDao.queryCoopChildList(paramMap1);
                if (childList != null && childList.size() > 0) {
                    persons.get(i).setHasChildren(true);
                } else {
                    persons.get(i).setHasChildren(false);
                }
            }
        }
        long countNums = Page.getTotal();//总记录数
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", countNums);
        rerultMap.put("dataList", persons);
        return rerultMap;
    }

    @Override
    public Map<String, Object> queryListForChoose(Map paramMap) {
        List<Map<String, Object>> list = coopDao.queryListForChoose(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public Map<String, Object> queryListForChooseForUpdate(Map paramMap) {
        List<Map<String, Object>> list = coopDao.queryListForChooseForUpdate(paramMap);
        Map<String, Object> rerultMap = new HashMap<String, Object>();
        rerultMap.put("totalNum", 0);
        rerultMap.put("dataList", list);
        return rerultMap;
    }

    @Override
    public Integer saveCoop(Coop coop) {
        //校验负责人是否黑名单和离职
        if(!"".equals(coop.getTransactor_id())){
            Integer countUser = coopDao.getUserCountByUserId(coop.getTransactor_id());
            if(countUser == 1){
                throw new RuntimeException("该负责人为黑名单或已离职，请更改");
            }
        }
        Integer coop_id = coopDao.saveCoop(coop);

        //默认插入 所有业务范畴
        List<String> list = coopDao.getBusinessScope();
        if(1 == SecurityUtils.getUser().getOrgId() && !list.contains("EF")){
            list.add("EF");
        }

        if(list!=null && list.size()>0){
            for(int i=0;i<list.size();i++){
                CoopScopeBean coopScope = new CoopScopeBean();
                coopScope.setCoop_id(coop.getCoop_id());
                coopScope.setCreator_id(SecurityUtils.getUser().getId());
                coopScope.setCreate_time(new Date());
                coopScope.setOrg_id(SecurityUtils.getUser().getOrgId());
                coopScope.setDept_id(SecurityUtils.getUser().getDeptId());
                coopScope.setBusiness_scope(list.get(i));
                coopScope.setIs_key_client(0);
                coopScope.setCredit_level("E级");
                coopScope.setCredit_limit(0.0);
                coopScope.setIncome_tax_rate(0.0);
                coopScope.setSettlement_period(30);
             /*   if("AE-TE".contains(coopScope.getBusiness_scope())) {
                	coopScope.setCredit_duration(30);
                }
                if("AI-SI-TI".contains(coopScope.getBusiness_scope())) {
                	coopScope.setCredit_duration(30);
                }*/
                if("SE".equals(coopScope.getBusiness_scope())) {
                	coopScope.setCredit_duration(60);
                }else{
                    coopScope.setCredit_duration(30);
                }
                coopScope.setScope_status(1);
                coopScope.setScope_id(null);
                coopScopeDao.saveCoopScope(coopScope);
            }
        }


        return coop_id;
    }

    @Override
    public Integer modifyCoop(Coop coop) {
        Integer coop_id = coopDao.modifyCoop(coop);

        return coop_id;
    }

    @Override
    public Coop viewCoop(Map paramMap) {
        Coop recoop = coopDao.viewCoop(paramMap);
        return recoop;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void modifyBlackWhiteValid(String coopId, String coopCode, String blackWhiteValid, String blackWhiteReason) {
        if (StrUtil.isBlank(coopCode)) {
            throw new RuntimeException("客商资料代码不可为空");
        }
        if (StrUtil.isBlank(blackWhiteValid)) {
            throw new RuntimeException("黑白名单标记不可为空");
        }
        if (StrUtil.isBlank(blackWhiteReason)) {
            throw new RuntimeException("黑白名单原因不可为空");
        }

        //修改子客商资料黑白名单
        coopDao.modifyBlackWhiteValidIncludeChildren(coopCode, blackWhiteValid, blackWhiteReason, SecurityUtils.getUser().getOrgId(), LocalDateTime.now());

        String type = "";
        String reason = "";
        if ("0".equals(blackWhiteValid)) {
            type = "加入黑名单";
            reason = "客商资料 [" + coopCode + "] 加入黑名单，原因：" + blackWhiteReason;
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        } else if ("1".equals(blackWhiteValid)) {
            type = "加入白名单";
            reason = "客商资料 [" + coopCode + "] 加入白名单，原因：" + blackWhiteReason;
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        }
        try {
            //保存日志
            HashMap<String, String> map = new HashMap<>();
            map.put("creator_name", "");
            map.put("op_type", type);
            map.put("op_name", "客商资料");
            map.put("op_level", "高");
            map.put("op_info", reason);
            saveLogger(map);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void modifyOutBlackWhiteValid(String coopId, String coopCode, String blackWhiteValid, String blackWhiteReason) {
        if (StrUtil.isBlank(coopCode)) {
            throw new RuntimeException("客商资料代码不可为空");
        }
        if (StrUtil.isBlank(blackWhiteValid)) {
            throw new RuntimeException("黑白名单标记不可为空");
        }
        if (StrUtil.isBlank(blackWhiteReason)) {
            throw new RuntimeException("移出黑白名单原因不可为空");
        }

        //修改子客商资料黑白名单
        coopDao.modifyOutBlackWhiteValidIncludeChildren(coopCode, blackWhiteValid, blackWhiteReason, SecurityUtils.getUser().getOrgId(), LocalDateTime.now());

        String type = "";
        String reason = "";
        if ("0".equals(blackWhiteValid)) {
            type = "移出黑名单";
            reason = "客商资料 [" + coopCode + "] 移出黑名单，原因：" + blackWhiteReason;
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        } else if ("1".equals(blackWhiteValid)) {
            type = "移出白名单";
            reason = "客商资料 [" + coopCode + "] 移出白名单，原因：" + blackWhiteReason;
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        }
        try {
            //保存日志
            HashMap<String, String> map = new HashMap<>();
            map.put("creator_name", "");
            map.put("op_type", type);
            map.put("op_name", "客商资料");
            map.put("op_level", "高");
            map.put("op_info", reason);
            saveLogger(map);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    //解锁、开锁
    @Override
    public void lockOrUnlock(String coopId, String coopCode, String coopStatus, String lockReason) {
        if (StrUtil.isBlank(coopId)) {
            throw new RuntimeException("客商资料id不可为空");
        }
        if (StrUtil.isBlank(coopCode)) {
            throw new RuntimeException("客商资料代码不可为空");
        }
        if (StrUtil.isBlank(coopStatus)) {
            throw new RuntimeException("解开锁标记不可为空");
        }
        if (StrUtil.isBlank(lockReason)) {
            throw new RuntimeException("解开锁原因不可为空");
        }
        String type = "";
        String reason = "";
        if ("0".equals(coopStatus)) {
            type = "锁定";
            reason = "客商资料 [" + coopCode + "] 被锁定，原因：" + lockReason;
            coopDao.lock(coopCode, lockReason, LocalDateTime.now(), SecurityUtils.getUser().getOrgId() + "");
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        } else if ("1".equals(coopStatus)) {
            type = "解锁";
            reason = "客商资料 [" + coopCode + "] 解锁，原因：" + lockReason;
            coopDao.unLock(coopId, lockReason);
            //更新异常表
            HashMap<String, Object> params = new HashMap<>();
            params.put("coopId", coopId);
            params.put("coopCode", coopCode);
            insertExceptionRecord(params, type, reason, false);
        }
        try {
            //保存日志
            HashMap<String, String> map = new HashMap<>();
            map.put("creator_name", "");
            map.put("op_type", type);
            map.put("op_name", "客商资料");
            map.put("op_level", "高");
            map.put("op_info", reason);
            saveLogger(map);
        } catch (Exception e) {
            log.info("客商资料" + coopCode + type + "成功,加入日志失败");
        }

    }

    @Override
    public List<CoopExcel> queryListForExcel(Map paramMap) {
        //bean.setOrg_id(SecurityUtils.getUser().getOrgId());
        return coopDao.queryListForExcel(paramMap);
    }

    //保存日志
    private void saveLogger(Map<String, String> map) {
        LogBean logBean = new LogBean();
        logBean.setOp_info(map.get("op_info"));
        logBean.setOp_level(map.get("op_level"));
        logBean.setOp_name(map.get("op_name"));
        logBean.setOp_type(map.get("op_type"));
        logBean.setCreator_name(map.get("creator_name"));
        logService.doSave(logBean);
    }


    @Override
    public void modifyCoopCode(String oriCoopCode, String afCoopCode) {
        coopDao.modifyCoopCode(oriCoopCode, afCoopCode, SecurityUtils.getUser().getOrgId() + "");
    }

    @Override
    public List<Coop> queryCoopCodes(String CoopCode) {
        return coopDao.queryCoopCodes(CoopCode, SecurityUtils.getUser().getOrgId() + "");
    }

    @Override
    public Coop queryCoopCodeByCoop(String CoopCode) {
        Coop recoop = coopDao.queryCoopCodeByCoop(CoopCode, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByCoopName(String CoopName) {
        Coop recoop = coopDao.queryCoopCodeByCoopName(CoopName, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByCoopName1(String CoopName, Integer coopId) {
        Coop recoop = coopDao.queryCoopCodeByCoopName1(CoopName, SecurityUtils.getUser().getOrgId() + "", coopId);
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByShortName(String ShortName) {
        Coop recoop = coopDao.queryCoopCodeByShortName(ShortName, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByShortName1(String ShortName, Integer coopId) {
        Coop recoop = coopDao.queryCoopCodeByShortName1(ShortName, SecurityUtils.getUser().getOrgId() + "", coopId);
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByCoopEName(String CoopEName) {
        Coop recoop = coopDao.queryCoopCodeByCoopEName(CoopEName, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByCoopEName1(String CoopEName, Integer coopId) {
        Coop recoop = coopDao.queryCoopCodeByCoopEName1(CoopEName, SecurityUtils.getUser().getOrgId() + "", coopId);
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByShortEName(String ShortEName) {
        Coop recoop = coopDao.queryCoopCodeByShortEName(ShortEName, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    @Override
    public Coop queryCoopCodeByShortEName1(String ShortEName, Integer coopId) {
        Coop recoop = coopDao.queryCoopCodeByShortEName1(ShortEName, SecurityUtils.getUser().getOrgId() + "", coopId);
        return recoop;
    }

    @Override
    public Integer isHaveSocialCreditCode(Coop coop) {
        coop.setOrg_id(SecurityUtils.getUser().getOrgId());
        Integer serviceCount = coopDao.isHaveSocialCreditCode(coop);
        return serviceCount;
    }

    @Override
    public List<Coop> listByType(Map<String, Object> paramMap) {
        return coopDao.listByType(paramMap);
    }

    @Override
    public List<Coop> listByCoopName(Map<String, Object> paramMap) {

        return coopDao.listByCoopName(paramMap);
    }

    @Override
    public Coop queryCoopCodeBySocialCreditCode(String SocialCreditCode) {
        Coop recoop = coopDao.queryCoopCodeBySocialCreditCode(SocialCreditCode, SecurityUtils.getUser().getOrgId() + "");
        return recoop;
    }

    private void insertExceptionRecord(Map<String, Object> params, String type, String reason, Boolean cascade) {
        if (cascade) {
            List<Coop> childrenCoop = coopDao.queryAllChildrenByCode(params.get("coopCode").toString(), SecurityUtils.getUser().getOrgId());
            childrenCoop.stream().forEach(coop -> {
                try {
                    CoopLockBean coopLockBean = new CoopLockBean();
                    coopLockBean.setCoop_id(coop.getCoop_id());
                    coopLockBean.setCreate_time(new Date());
                    coopLockBean.setCreator_id(SecurityUtils.getUser().getId());
                    coopLockBean.setLock_id(null);
                    coopLockBean.setOrg_id(SecurityUtils.getUser().getOrgId());
                    coopLockBean.setLock_type(type);
                    int index = reason.indexOf("&&");
                    String reasonResult = "";
                    if (params.get("coopCode").toString().equals(coop.getCoop_code())) {
                        reasonResult = reason.substring(0, index) + reason.substring(index + 2);
                    } else {
                        reasonResult = reason.substring(0, index) + " [" + coop.getCoop_code() + "] " + reason.substring(index + 2);
                    }
                    coopLockBean.setLock_reason(reasonResult);
                    coopLockService.saveCoopLock(coopLockBean);
                } catch (Exception e) {
                    log.info("客商资料" + params.get("coopCode").toString() + type + "时，下级客商资料" + coop.getCoop_code() + "加入异常表失败");
                }
            });
        } else {
            try {
                CoopLockBean coopLockBean = new CoopLockBean();
                coopLockBean.setCoop_id(Integer.parseInt(params.get("coopId").toString()));
                coopLockBean.setCreate_time(new Date());
                coopLockBean.setCreator_id(SecurityUtils.getUser().getId());
                coopLockBean.setLock_id(null);
                coopLockBean.setOrg_id(SecurityUtils.getUser().getOrgId());
                coopLockBean.setLock_type(type);
                coopLockBean.setLock_reason(reason);
                coopLockService.saveCoopLock(coopLockBean);
            } catch (Exception e) {
                log.info("客商资料" + params.get("coopCode").toString() + type + "时加入异常表失败");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(List<Coop> data) {
        if(data != null && data.size()>0){
            for(int i=0;i<data.size();i++){
                Coop coop = data.get(i);
                //插入客商资料信息开始
                coop.setCoop_code(coop.getCoop_code().toUpperCase());
                coop.setCoop_mnemonic(coop.getCoop_code().toUpperCase());
                coop.setShort_name(coop.getCoop_name());
                coop.setShort_ename(coop.getCoop_ename());
                coop.setCreator_id(SecurityUtils.getUser().getId());
                coop.setCreate_time(new Date());
                coop.setOrg_id(SecurityUtils.getUser().getOrgId());
                coop.setDept_id(SecurityUtils.getUser().getDeptId());
                coop.setGroup_type("file");
                Integer coop_id = coopDao.saveCoop(coop);
                //插入客商资料信息结束
                //插入业务范畴信息开始
                List<String> list = coopDao.getBusinessScope();//默认插入 所有业务范畴

                if(list!=null && list.size()>0){
                    for(int j=0;j<list.size();j++){
                        CoopScopeBean coopScope = new CoopScopeBean();
                        coopScope.setCoop_id(coop.getCoop_id());
                        coopScope.setCreator_id(SecurityUtils.getUser().getId());
                        coopScope.setCreate_time(new Date());
                        coopScope.setOrg_id(SecurityUtils.getUser().getOrgId());
                        coopScope.setDept_id(SecurityUtils.getUser().getDeptId());
                        coopScope.setBusiness_scope(list.get(j));
                        coopScope.setIs_key_client(0);
                        coopScope.setCredit_level("E级");
                        coopScope.setCredit_limit(0.0);
                        coopScope.setIncome_tax_rate(0.0);
                        coopScope.setSettlement_period(30);
                        if("SE".equals(coopScope.getBusiness_scope())) {
                            coopScope.setCredit_duration(60);
                        }else{
                            coopScope.setCredit_duration(30);
                        }
                        coopScope.setScope_status(1);
                        coopScope.setScope_id(null);
                        coopScopeDao.saveCoopScope(coopScope);
                    }
                }
                //插入业务范畴信息结束
                //插入注册地址信息开始
                if(!"".equals(coop.getFull_address())){
                    CoopAddressBean coopAddress = new CoopAddressBean();
                    coopAddress.setCoop_id(coop.getCoop_id());
                    coopAddress.setCreator_id(SecurityUtils.getUser().getId());
                    coopAddress.setCreate_time(new Date());
                    coopAddress.setOrg_id(SecurityUtils.getUser().getOrgId());
                    coopAddress.setDept_id(SecurityUtils.getUser().getDeptId());
                    coopAddress.setShort_name(coop.getFull_address());
                    coopAddress.setFull_address(coop.getFull_address());
                    coopAddress.setAddr_type("注册地址");
                    coopAddress.setAddr_status(1);
                    coopAddressDao.saveCoopAddress(coopAddress);
                }
                //插入注册地址信息结束
                //插入联系人信息开始
                if(!"".equals(coop.getContacts_name1()) && !"".equals(coop.getEmail())){
                    CoopContactsBean coopContacts = new CoopContactsBean();
                    coopContacts.setCoop_id(coop.getCoop_id());
                    coopContacts.setContacts_type("对账");
                    coopContacts.setContacts_name(coop.getContacts_name1());
                    coopContacts.setPhone_number(coop.getPhone_number1());
                    coopContacts.setEmail(coop.getEmail());
                    coopContacts.setDept_name(coop.getDept_name());
                    coopContacts.setJob_position(coop.getJob_position());
                    coopContacts.setCreator_id(SecurityUtils.getUser().getId());
                    coopContacts.setCreate_time(new Date());
                    coopContacts.setOrg_id(SecurityUtils.getUser().getOrgId());
                    coopContacts.setDept_id(SecurityUtils.getUser().getDeptId());
                    coopContacts.setContacts_status(1);
                    coopContactsDao.saveCoopContacts(coopContacts);
                }
                //插入联系人信息结束
            }
        }
    }

    @Override
    public String downloadTemplate(){
        String lastFilePath = PDFUtils.filePath + "/PDFtemplate/客商资料导入.xlsx";
        return lastFilePath.replace(PDFUtils.filePath, "");
    }

    @Override
    public Coop getCoopCountByCode(String coopCode,Integer orgId) {

        return coopDao.queryCoopCodeByCoop(coopCode,orgId+ "");
    }

    @Override
    public Coop getCoopCountByName(String coopName,Integer orgId) {

        return coopDao.queryCoopCodeByCoopName(coopName,orgId+ "");
    }

    @Override
    public Integer saveCoop1(Coop coop) {
        //校验负责人是否黑名单和离职
        if(!"".equals(coop.getTransactor_id())){
            Integer countUser = coopDao.getUserCountByUserId(coop.getTransactor_id());
            if(countUser == 1){
                throw new RuntimeException("该负责人为黑名单或已离职，请更改");
            }
        }
        Integer coop_id = coopDao.saveCoop(coop);

        //默认插入 所有业务范畴
        List<String> list = coopDao.getBusinessScope();

        if(list!=null && list.size()>0){
            for(int i=0;i<list.size();i++){
                CoopScopeBean coopScope = new CoopScopeBean();
                coopScope.setCoop_id(coop.getCoop_id());
                coopScope.setCreator_id(SecurityUtils.getUser().getId());
                coopScope.setCreate_time(new Date());
                coopScope.setOrg_id(coop.getOrg_id());
                coopScope.setDept_id(SecurityUtils.getUser().getDeptId());
                coopScope.setBusiness_scope(list.get(i));
                coopScope.setIs_key_client(0);
                coopScope.setCredit_level("E级");
                coopScope.setCredit_limit(0.0);
                coopScope.setIncome_tax_rate(0.0);
                coopScope.setSettlement_period(30);
                if("SE".equals(coopScope.getBusiness_scope())) {
                    coopScope.setCredit_duration(60);
                }else{
                    coopScope.setCredit_duration(30);
                }
                coopScope.setScope_status(1);
                coopScope.setScope_id(null);
                coopScopeDao.saveCoopScope(coopScope);
            }
        }
        return coop_id;
    }

    @Override
    public List<Coop> selectPrmCoopsForAwb(Integer orgId, String bussinessScope) {
        return coopDao.selectPrmCoopsForAwb(orgId,bussinessScope);
    }
}

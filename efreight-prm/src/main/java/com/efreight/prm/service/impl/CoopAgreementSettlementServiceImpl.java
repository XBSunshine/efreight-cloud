package com.efreight.prm.service.impl;

import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.dao.CoopAgreementSettlementDao;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.CoopAgreementSettlementService;
import com.efreight.prm.service.LogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@AllArgsConstructor
@Service
public class CoopAgreementSettlementServiceImpl implements CoopAgreementSettlementService {

    private final CoopAgreementSettlementDao coopAgreementSettlementDao;
    private final LogService logService;

    /**
     * 有条件查询列表数据
     *
     * @param coopAgreementSettlement
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map<String, Object> findCoopAgreementSettlementListCriteria(CoopAgreementSettlement coopAgreementSettlement, Integer currentPage, Integer pageSize) {
        Page<CoopAgreementSettlement> page = PageHelper.startPage(currentPage, pageSize);
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        if(coopAgreementSettlement.getSettlementState()!=null && !"".equals(coopAgreementSettlement.getSettlementState()) && "已超期".equals(coopAgreementSettlement.getSettlementState())){
            coopAgreementSettlement.setOverDate(new Date());
        }
        List<CoopAgreementSettlement> paramList = coopAgreementSettlementDao.findCoopAgreementSettlementListCriteriaGroup(coopAgreementSettlement);
        //根据coopId查询子级
        if(paramList!=null && paramList.size()>0){
            for(int i=0;i<paramList.size();i++){
                paramList.get(i).setId(UUID.randomUUID().toString());
                HashMap<String, Object> serachMap = new HashMap<>();
                serachMap.put("orgId", SecurityUtils.getUser().getOrgId());
                serachMap.put("settlementId", paramList.get(i).getSettlementId());
                if(coopAgreementSettlement.getNeedEmail()!=null && !"".equals(coopAgreementSettlement.getNeedEmail())){
                    serachMap.put("needEmail", coopAgreementSettlement.getNeedEmail());
                }
                if(coopAgreementSettlement.getSettlementModName()!=null && !"".equals(coopAgreementSettlement.getSettlementModName())){
                    serachMap.put("settlementModName", coopAgreementSettlement.getSettlementModName());
                }
                if(coopAgreementSettlement.getQuantityConfirmName()!=null && !"".equals(coopAgreementSettlement.getQuantityConfirmName())){
                    serachMap.put("quantityConfirmName", coopAgreementSettlement.getQuantityConfirmName());
                }
                if(coopAgreementSettlement.getBillConfirmName()!=null && !"".equals(coopAgreementSettlement.getBillConfirmName())){
                    serachMap.put("billConfirmName", coopAgreementSettlement.getBillConfirmName());
                }
                if(coopAgreementSettlement.getHeadOfficeConfirmName()!=null && !"".equals(coopAgreementSettlement.getHeadOfficeConfirmName())){
                    serachMap.put("headOfficeConfirmName", coopAgreementSettlement.getHeadOfficeConfirmName());
                }
                if(coopAgreementSettlement.getSettlementState()!=null && !"".equals(coopAgreementSettlement.getSettlementState())){
                    serachMap.put("settlementState", coopAgreementSettlement.getSettlementState());
                }
                if(coopAgreementSettlement.getSettlementState()!=null && !"".equals(coopAgreementSettlement.getSettlementState()) && "已超期".equals(coopAgreementSettlement.getSettlementState())){
                    serachMap.put("overDate", new Date());
                }
                CoopAgreementSettlement cas=paramList.get(i);
                cas.setOrgId(SecurityUtils.getUser().getOrgId());
                List<CoopAgreementSettlementDetail> coopAgreementSettlementDetailList = coopAgreementSettlementDao.findCoopAgreementSettlementListCriteriaDetail(serachMap);
                if(coopAgreementSettlementDetailList!=null && coopAgreementSettlementDetailList.size()>0){
                    for(int j=0;j<coopAgreementSettlementDetailList.size();j++){
                        coopAgreementSettlementDetailList.get(j).setId(UUID.randomUUID().toString());
                        if(coopAgreementSettlementDetailList.get(j).getSettlementState()!=null && !"".equals(coopAgreementSettlementDetailList.get(j).getSettlementState()) && "待审核".equals(coopAgreementSettlementDetailList.get(j).getSettlementState())){
                            paramList.get(i).setSettlementState("待审核");
                        }
                    }
                }
                //查询对账联系人
                List<Integer> billConfirmContacts = coopAgreementSettlementDao.findContactsIdList(cas);
                paramList.get(i).setInvoiceReceiveEmails(billConfirmContacts);
                paramList.get(i).setCoopAgreementSettlementDetail(coopAgreementSettlementDetailList);
            }
        }
        Integer totalNum = Integer.parseInt(page.getTotal() + "");
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("totalNum", totalNum);
        resultMap.put("paramList", paramList);
        return resultMap;
    }

    /**
     * 删除
     *
     * @param coopAgreementSettlement
     */
    @Override
    public void deleteCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        coopAgreementSettlementDao.deleteCoopAgreementSettlement(coopAgreementSettlement);
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("删除");
            bean.setOp_level("高");
            bean.setOp_name("客商资料结算协议");
            bean.setOp_info("结算协议" + coopAgreementSettlement.getSettlementId() + "被删除");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("结算协议删除成功,日志更新失败!");
        }
    }

    @Override
    public void validCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setEditTime(new Date());
        coopAgreementSettlement.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        coopAgreementSettlement.setReviewFinanceTime(new Date());
        coopAgreementSettlement.setReviewFinanceName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        coopAgreementSettlement.setReviewFinance(1);

        if(coopAgreementSettlement.getGroupId()!=null && !"".equals(coopAgreementSettlement.getGroupId())){//审核明细
            //根据settlementId查询明细信息
            CoopAgreementSettlement cas = coopAgreementSettlementDao.getCoopAgreementSettlementDetailById(coopAgreementSettlement.getSettlementId());

            if(cas != null){
                if(cas.getReviewItNeed1() == 0){//需要IT审核 =0 ，则 状态 = 已生效
                    coopAgreementSettlement.setSettlementState("已生效");
                }else if(cas.getReviewItNeed1() == 1 && cas.getReviewIt() == 1){//需要IT审核 =1 且 IT审核 =1 ，则 状态 = 已生效
                    coopAgreementSettlement.setSettlementState("已生效");
                }else{
                    coopAgreementSettlement.setSettlementState(cas.getSettlementState());
                }
                //状态为待审核的才能进行审核
                if("待审核".equals(cas.getSettlementState())){
                    coopAgreementSettlementDao.validCoopAgreementSettlement(coopAgreementSettlement);
                }
            }

        }else{
            if(coopAgreementSettlement.getSettlementIdDetails()!=null && !"".equals(coopAgreementSettlement.getSettlementIdDetails())){
                String[] settlementIdDetails = coopAgreementSettlement.getSettlementIdDetails().split(",");
                if(settlementIdDetails!=null && settlementIdDetails.length>0){
                    for(int i=0;i<settlementIdDetails.length;i++){
                        coopAgreementSettlement.setSettlementId(Integer.parseInt(settlementIdDetails[i]));
                        //根据settlementId查询明细信息
                        CoopAgreementSettlement cas = coopAgreementSettlementDao.getCoopAgreementSettlementDetailById(coopAgreementSettlement.getSettlementId());

                        if(cas != null){
                            if(cas.getReviewItNeed1() == 0){//需要IT审核 =0 ，则 状态 = 已生效
                                coopAgreementSettlement.setSettlementState("已生效");
                            }else if(cas.getReviewItNeed1() == 1 && cas.getReviewIt() == 1){//需要IT审核 =1 且 IT审核 =1 ，则 状态 = 已生效
                                coopAgreementSettlement.setSettlementState("已生效");
                            }else{
                                coopAgreementSettlement.setSettlementState(cas.getSettlementState());
                            }
                            //状态为待审核的才能进行审核
                            if("待审核".equals(cas.getSettlementState())){
                                coopAgreementSettlementDao.validCoopAgreementSettlement(coopAgreementSettlement);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void validItCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setEditTime(new Date());
        coopAgreementSettlement.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        coopAgreementSettlement.setReviewItTime(new Date());
        coopAgreementSettlement.setReviewItName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        coopAgreementSettlement.setReviewIt(1);

        if(coopAgreementSettlement.getGroupId()!=null && !"".equals(coopAgreementSettlement.getGroupId())){//审核明细
            //根据settlementId查询明细信息
            CoopAgreementSettlement cas = coopAgreementSettlementDao.getCoopAgreementSettlementDetailById(coopAgreementSettlement.getSettlementId());

            if(cas != null){
                if(cas.getReviewItNeed1() == 1 && cas.getReviewFinance() == 1){// 需要IT审核 =1 且 财务审核 =1，则 状态 = 已生效
                    coopAgreementSettlement.setSettlementState("已生效");
                }else{
                    coopAgreementSettlement.setSettlementState(cas.getSettlementState());
                }
                //状态为待审核的才能进行审核
                if("待审核".equals(cas.getSettlementState())){
                    coopAgreementSettlementDao.validItCoopAgreementSettlement(coopAgreementSettlement);
                }
            }

        }else{
            if(coopAgreementSettlement.getSettlementIdDetails()!=null && !"".equals(coopAgreementSettlement.getSettlementIdDetails())){
                String[] settlementIdDetails = coopAgreementSettlement.getSettlementIdDetails().split(",");
                if(settlementIdDetails!=null && settlementIdDetails.length>0){
                    for(int i=0;i<settlementIdDetails.length;i++){
                        coopAgreementSettlement.setSettlementId(Integer.parseInt(settlementIdDetails[i]));
                        //根据settlementId查询明细信息
                        CoopAgreementSettlement cas = coopAgreementSettlementDao.getCoopAgreementSettlementDetailById(coopAgreementSettlement.getSettlementId());

                        if(cas != null){
                            if(cas.getReviewItNeed1() == 1 && cas.getReviewFinance() == 1){// 需要IT审核 =1 且 财务审核 =1，则 状态 = 已生效
                                coopAgreementSettlement.setSettlementState("已生效");
                            }else{
                                coopAgreementSettlement.setSettlementState(cas.getSettlementState());
                            }
                            //状态为待审核的并且需要IT审核才能进行审核
                            if("待审核".equals(cas.getSettlementState()) && cas.getReviewItNeed1() == 1){
                                coopAgreementSettlementDao.validItCoopAgreementSettlement(coopAgreementSettlement);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void invalidCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setEditTime(new Date());
        coopAgreementSettlement.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        if(coopAgreementSettlement.getGroupId()!=null && !"".equals(coopAgreementSettlement.getGroupId())){
            coopAgreementSettlementDao.invalidCoopAgreementSettlement(coopAgreementSettlement);
        }else{
            if(coopAgreementSettlement.getSettlementIdDetails()!=null && !"".equals(coopAgreementSettlement.getSettlementIdDetails())){
                String[] settlementIdDetails = coopAgreementSettlement.getSettlementIdDetails().split(",");
                if(settlementIdDetails!=null && settlementIdDetails.length>0){
                    for(int i=0;i<settlementIdDetails.length;i++){
                        coopAgreementSettlement.setSettlementId(Integer.parseInt(settlementIdDetails[i]));
                        coopAgreementSettlementDao.invalidCoopAgreementSettlement(coopAgreementSettlement);
                    }
                }
            }
        }
    }


    /**
     * 新建
     *
     * @param coopAgreementSettlement
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        //非空判断
        if (StringUtils.isEmpty(coopAgreementSettlement.getSettlementModName())) {
            throw new RuntimeException("模板名称不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getSettlementType())) {
            throw new RuntimeException("计费模式不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getSettlementPeriod())) {
            throw new RuntimeException("结算周期不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getBillConfirmId())) {
            throw new RuntimeException("账单确认责任人不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getQuantityConfirmId())) {
            throw new RuntimeException("数据填充责任人不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getPaymentMethod())) {
            throw new RuntimeException("结算方式不能为空");
        }
        if (StringUtils.isEmpty(coopAgreementSettlement.getSettlementModBillMonth())) {
            throw new RuntimeException("发送账单月份不能为空");
        }

        //插入新建时间
        Date currDate = new Date();
        coopAgreementSettlement.setCreatorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setCreateTime(currDate);
        coopAgreementSettlement.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        //coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        //coopAgreementSettlement.setEditTime(currDate);
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        coopAgreementSettlement.setDeptId(SecurityUtils.getUser().getDeptId());
        //设置开始日期的格式为yyyy-MM-dd HH:mm:ss
        if(coopAgreementSettlement.getBeginDate()!=null && !"".equals(coopAgreementSettlement.getBeginDate())){
            coopAgreementSettlement.setBeginDate(coopAgreementSettlement.getBeginDate()+"-01 00:00:00");
        }
        if(coopAgreementSettlement.getEndDate()!=null && !"".equals(coopAgreementSettlement.getEndDate())){
            String enddate=coopAgreementSettlement.getEndDate();
            int year=Integer.parseInt(enddate.substring(0,4));
            int month=Integer.parseInt(enddate.substring(5,7));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month-1);
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, lastDay);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String lastDayOfMonth = sdf.format(cal.getTime());
            coopAgreementSettlement.setEndDate(lastDayOfMonth+" 23:59:59");
        }
        //设置是否需要IT审核
        if(coopAgreementSettlement.getReviewItNeed()!=null && coopAgreementSettlement.getReviewItNeed() == true){
            coopAgreementSettlement.setReviewItNeed1(1);
        }else{
            coopAgreementSettlement.setReviewItNeed1(0);
        }
        coopAgreementSettlementDao.createCoopAgreementSettlement(coopAgreementSettlement);
        //向prm_coop_agreement_settlement_contacts表插入对账联系人
        if(coopAgreementSettlement.getBillConfirmContacts()!=null && coopAgreementSettlement.getBillConfirmContacts().size()>0){
            for (int i = 0; i < coopAgreementSettlement.getBillConfirmContacts().size(); i++) {
                coopAgreementSettlement.setBillConfirmContacts1(coopAgreementSettlement.getBillConfirmContacts().get(i));
                coopAgreementSettlementDao.insertBillConfirmContacts(coopAgreementSettlement);
            }
        }
        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("新建");
            bean.setOp_level("高");
            bean.setOp_name("客商资料结算协议");
            bean.setOp_info("结算协议" + coopAgreementSettlement.getAgreementId() + "新建");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("结算协议新建成功,日志更新失败!");
        }
    }

    /**
     * 新建
     *
     * @param coopAgreementSettlement
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCoopAgreementSettlementGroup(CoopAgreementSettlement coopAgreementSettlement) {

        //插入新建时间
        Date currDate = new Date();
        coopAgreementSettlement.setCreatorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setCreateTime(currDate);
        coopAgreementSettlement.setCreatorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        //coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        //coopAgreementSettlement.setEditTime(currDate);
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        coopAgreementSettlement.setDeptId(SecurityUtils.getUser().getDeptId());
        //设置开始日期的格式为yyyy-MM-dd HH:mm:ss
        if(coopAgreementSettlement.getBeginDate()!=null && !"".equals(coopAgreementSettlement.getBeginDate())){
            coopAgreementSettlement.setBeginDate(coopAgreementSettlement.getBeginDate()+"-01 00:00:00");
        }
        if(coopAgreementSettlement.getEndDate()!=null && !"".equals(coopAgreementSettlement.getEndDate())){
            String enddate=coopAgreementSettlement.getEndDate();
            int year=Integer.parseInt(enddate.substring(0,4));
            int month=Integer.parseInt(enddate.substring(5,7));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month-1);
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, lastDay);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String lastDayOfMonth = sdf.format(cal.getTime());
            coopAgreementSettlement.setEndDate(lastDayOfMonth+" 23:59:59");
        }
        coopAgreementSettlementDao.createCoopAgreementSettlementGroup(coopAgreementSettlement);

        //向prm_coop_agreement_settlement_contacts表插入电子发票接收邮箱
        if(coopAgreementSettlement.getInvoiceReceiveEmails()!=null && coopAgreementSettlement.getInvoiceReceiveEmails().size()>0){
            for (int i = 0; i < coopAgreementSettlement.getInvoiceReceiveEmails().size(); i++) {
                coopAgreementSettlement.setBillConfirmContacts1(coopAgreementSettlement.getInvoiceReceiveEmails().get(i));
                coopAgreementSettlementDao.insertBillConfirmContacts(coopAgreementSettlement);
            }
        }
    }

    /**
     * 编辑
     *
     * @param coopAgreementSettlement
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editCoopAgreementSettlementGroup(CoopAgreementSettlement coopAgreementSettlement) {

        //插入新建时间
        Date currDate = new Date();
        //coopAgreementSettlement.setCreatorId(SecurityUtils.getUser().getId());
        //coopAgreementSettlement.setCreateTime(currDate);
        coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setEditTime(currDate);
        coopAgreementSettlement.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        //coopAgreementSettlement.setDeptId(SecurityUtils.getUser().getDeptId());
        //设置开始日期的格式为yyyy-MM-dd HH:mm:ss
        if(coopAgreementSettlement.getBeginDate()!=null){
            coopAgreementSettlement.setBeginDate(coopAgreementSettlement.getBeginDate().substring(0,7)+"-01 00:00:00");
        }
        if(coopAgreementSettlement.getEndDate()!=null && !"".equals(coopAgreementSettlement.getEndDate())){
            String enddate=coopAgreementSettlement.getEndDate();
            int year=Integer.parseInt(enddate.substring(0,4));
            int month=Integer.parseInt(enddate.substring(5,7));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month-1);
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, lastDay);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String lastDayOfMonth = sdf.format(cal.getTime());
            coopAgreementSettlement.setEndDate(lastDayOfMonth+" 23:59:59");
        }
        coopAgreementSettlementDao.editCoopAgreementSettlementGroup(coopAgreementSettlement);

        if(coopAgreementSettlement.getInvoiceReceiveEmails()!=null && coopAgreementSettlement.getInvoiceReceiveEmails().size()>0){
            //联系人先删除
            coopAgreementSettlementDao.deleteBillConfirmContacts(coopAgreementSettlement);
            //向prm_coop_agreement_settlement_contacts表插入对账联系人
            for (int i = 0; i < coopAgreementSettlement.getInvoiceReceiveEmails().size(); i++) {
                coopAgreementSettlement.setBillConfirmContacts1(coopAgreementSettlement.getInvoiceReceiveEmails().get(i));
                coopAgreementSettlementDao.insertBillConfirmContacts(coopAgreementSettlement);
            }
        }else{
            coopAgreementSettlementDao.deleteBillConfirmContacts(coopAgreementSettlement);
        }
    }

    /**
     * 查询单个参数
     *
     * @param coopAgreementSettlement
     * @return
     */
    @Override
    public CoopAgreementSettlement findCoopAgreementSettlementCriteria(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        CoopAgreementSettlement cas = coopAgreementSettlementDao.findCoopAgreementSettlementCriteria(coopAgreementSettlement);
        //查询对账联系人
        List<Integer> billConfirmContacts = coopAgreementSettlementDao.findContactsIdList(coopAgreementSettlement);
        cas.setBillConfirmContacts(billConfirmContacts);
        return cas;
    }

    /**
     * 修改
     *
     * @param coopAgreementSettlement
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyCoopAgreementSettlement(CoopAgreementSettlement coopAgreementSettlement) {
        coopAgreementSettlement.setOrgId(SecurityUtils.getUser().getOrgId());
        coopAgreementSettlement.setDeptId(SecurityUtils.getUser().getDeptId());
        coopAgreementSettlement.setEditorId(SecurityUtils.getUser().getId());
        coopAgreementSettlement.setEditTime(new Date());
        coopAgreementSettlement.setEditorName(SecurityUtils.getUser().getUserCname()+" "+SecurityUtils.getUser().getUserEmail());
        //设置开始日期的格式为yyyy-MM-dd HH:mm:ss
        if(coopAgreementSettlement.getBeginDate()!=null && !"".equals(coopAgreementSettlement.getBeginDate())){
            coopAgreementSettlement.setBeginDate(coopAgreementSettlement.getBeginDate().substring(0,7)+"-01 00:00:00");
        }
        if(coopAgreementSettlement.getEndDate()!=null && !"".equals(coopAgreementSettlement.getEndDate())){
            String enddate=coopAgreementSettlement.getEndDate();
            int year=Integer.parseInt(enddate.substring(0,4));
            int month=Integer.parseInt(enddate.substring(5,7));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month-1);
            int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, lastDay);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String lastDayOfMonth = sdf.format(cal.getTime());
            coopAgreementSettlement.setEndDate(lastDayOfMonth+" 23:59:59");
        }
        //设置是否需要IT审核
        if(coopAgreementSettlement.getReviewItNeed()!=null && coopAgreementSettlement.getReviewItNeed() == true){
            coopAgreementSettlement.setReviewItNeed1(1);
        }else{
            coopAgreementSettlement.setReviewItNeed1(0);
        }
        coopAgreementSettlementDao.modifyCoopAgreementSettlement(coopAgreementSettlement);
        if(coopAgreementSettlement.getBillConfirmContacts()!=null && coopAgreementSettlement.getBillConfirmContacts().size()>0){
            //联系人先删除
            coopAgreementSettlementDao.deleteBillConfirmContacts(coopAgreementSettlement);
            //向prm_coop_agreement_settlement_contacts表插入对账联系人
            for (int i = 0; i < coopAgreementSettlement.getBillConfirmContacts().size(); i++) {
                coopAgreementSettlement.setBillConfirmContacts1(coopAgreementSettlement.getBillConfirmContacts().get(i));
                coopAgreementSettlementDao.insertBillConfirmContacts(coopAgreementSettlement);
            }
        }


        try {
            LogBean bean = new LogBean();
            bean.setOrg_id(SecurityUtils.getUser().getOrgId());
            bean.setDept_id(SecurityUtils.getUser().getDeptId());
            bean.setCreator_id(SecurityUtils.getUser().getId());
            bean.setCreate_time(new Date());
            bean.setCreator_name("");
            bean.setOp_type("修改");
            bean.setOp_level("高");
            bean.setOp_name("客商资料结算协议");
            bean.setOp_info("结算协议" + coopAgreementSettlement.getSettlementId() + "修改");
            logService.doSave(bean);
        } catch (Exception e) {
            throw new RuntimeException("结算协议修改成功,日志更新失败!");
        }
    }

    @Override
    public List<CoopAgreementSettlementExcel> queryListForExcel(CoopAgreementSettlement coopAgreementSettlement) {
        if(coopAgreementSettlement.getSettlementState()!=null && !"".equals(coopAgreementSettlement.getSettlementState()) && "已超期".equals(coopAgreementSettlement.getSettlementState())){
            coopAgreementSettlement.setOverDate(new Date());
        }
        return coopAgreementSettlementDao.queryListForExcel(coopAgreementSettlement);
    }

    @Override
    public List<CoopAgreementSettlement> selectGoodStatusList(Date createTimeStart, Date createTimeEnd, Integer orgId) {
        return coopAgreementSettlementDao.selectGoodStatusList(createTimeStart,createTimeEnd,orgId);
    }
    @Override
    public List<CoopAgreementSettlement> selectMonthBillList(Date createTimeStart, Date createTimeEnd, Integer orgId) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	map.put("createTimeStart", String.format("%tY", createTimeStart)+"-"+String .format("%tm", createTimeStart));
    	map.put("orgId", orgId);
    	return coopAgreementSettlementDao.selectMonthBillList(map);
    }
    @Override
    public List<CoopAgreementSettlement> selectYearBillList(Date createTimeStart, Date createTimeEnd, Integer orgId) {
    	Map<String, Object> map=new HashMap<String, Object>();
    	map.put("createTimeStart", String.format("%tY", createTimeStart)+"-"+String .format("%tm", createTimeStart));
    	map.put("orgId", orgId);
    	return coopAgreementSettlementDao.selectYearBillList(map);
    }

    @Override
    public List<FlightOptionsBean> selectFlightOptions() {
        return coopAgreementSettlementDao.selectFlightOptions();
    }
}

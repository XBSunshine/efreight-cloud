package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.*;
import com.efreight.afbase.entity.*;
import com.efreight.afbase.entity.exportExcel.IncomeExcel;
import com.efreight.afbase.service.*;
import com.efreight.afbase.utils.FieldValUtils;
import com.efreight.common.core.feign.RemoteCoopService;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FormatUtils;
import com.efreight.common.core.utils.WebUtils;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.CoopVo;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 延伸服务 应收 服务实现类
 * </p>
 *
 * @author qipm
 * @since 2019-10-12
 */
@Service
@AllArgsConstructor
public class AfIncomeServiceImpl extends ServiceImpl<AfIncomeMapper, AfIncome> implements AfIncomeService {
    private final AfCostMapper costMapper;
    private final ScIncomeMapper scIncomeMapper;
    private final TcIncomeMapper tcIncomeMapper;
    private final ScCostMapper scCostMapper;
    private final LcIncomeMapper lcIncomeMapper;
    private final IoIncomeMapper ioIncomeMapper;
    
    private final TcOrderService tcOrderService;

    private final AfOrderService afOrderService;
    private final ScOrderService scOrderService;
    private final LcOrderService lcOrderService;
    private final RemoteCoopService remoteCoopService;
    private final IoOrderService ioOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doSave(AfIncome bean) {
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            bean.setCreateTime(LocalDateTime.now());
            bean.setCreatorId(SecurityUtils.getUser().getId());
            bean.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            bean.setOrgId(SecurityUtils.getUser().getOrgId());
            baseMapper.insert(bean);
            for (int i = 0; i < bean.getCosts().size(); i++) {
                AfCost cost = bean.getCosts().get(i);
                cost.setIncomeId(bean.getIncomeId());
                cost.setCreateTime(LocalDateTime.now());
                cost.setCreatorId(SecurityUtils.getUser().getId());
                cost.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                cost.setOrgId(SecurityUtils.getUser().getOrgId());
                costMapper.insert(cost);
            }
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            ScIncome bean2 = new ScIncome();
            BeanUtils.copyProperties(bean, bean2);
            bean2.setCreateTime(LocalDateTime.now());
            bean2.setCreatorId(SecurityUtils.getUser().getId());
            bean2.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            bean2.setOrgId(SecurityUtils.getUser().getOrgId());
            scIncomeMapper.insert(bean2);
            for (int i = 0; i < bean.getCosts().size(); i++) {
                AfCost cost = bean.getCosts().get(i);
                ScCost cost2 = new ScCost();
                BeanUtils.copyProperties(cost, cost2);
                cost2.setIncomeId(bean2.getIncomeId());
                cost2.setCreateTime(LocalDateTime.now());
                cost2.setCreatorId(SecurityUtils.getUser().getId());
                cost2.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                cost2.setOrgId(SecurityUtils.getUser().getOrgId());
                scCostMapper.insert(cost2);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doUpdate(AfIncome bean) {
        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            bean.setEditTime(LocalDateTime.now());
            bean.setEditorId(SecurityUtils.getUser().getId());
            bean.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            UpdateWrapper<AfIncome> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("income_id", bean.getIncomeId());
            if (bean.getDebitNoteId() == null || "".equals(bean.getDebitNoteId())) {
                baseMapper.update(bean, updateWrapper);
            }
            baseMapper.deleteCostByIncomeId2(SecurityUtils.getUser().getOrgId(), bean.getIncomeId());
            for (int i = 0; i < bean.getCosts().size(); i++) {
                AfCost cost = bean.getCosts().get(i);
                cost.setIncomeId(bean.getIncomeId());

                if (cost.getCreatorId() == null) {
                    cost.setCreateTime(LocalDateTime.now());
                    cost.setCreatorId(SecurityUtils.getUser().getId());
                    cost.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                }

                cost.setOrgId(SecurityUtils.getUser().getOrgId());
                if ("1".equals(bean.getIsFlag())) {
                    cost.setEditTime(LocalDateTime.now());
                    cost.setEditorId(SecurityUtils.getUser().getId());
                    cost.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                }

                if (cost.getPaymentId() == null || "".equals(cost.getPaymentId())) {
                    costMapper.insert(cost);
                }
            }
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            ScIncome bean2 = new ScIncome();
            BeanUtils.copyProperties(bean, bean2);
            bean2.setEditTime(LocalDateTime.now());
            bean2.setEditorId(SecurityUtils.getUser().getId());
            bean2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
            UpdateWrapper<ScIncome> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("income_id", bean2.getIncomeId());
            if (bean.getDebitNoteId() == null || "".equals(bean.getDebitNoteId())) {
                scIncomeMapper.update(bean2, updateWrapper);
            }
            baseMapper.deleteCostBySEIncomeId(SecurityUtils.getUser().getOrgId(), bean.getIncomeId());
            for (int i = 0; i < bean.getCosts().size(); i++) {
                AfCost cost = bean.getCosts().get(i);
                ScCost cost2 = new ScCost();
                BeanUtils.copyProperties(cost, cost2);
                cost2.setIncomeId(bean.getIncomeId());

                if (cost2.getCreatorId() == null) {
                    cost2.setCreateTime(LocalDateTime.now());
                    cost2.setCreatorId(SecurityUtils.getUser().getId());
                    cost2.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                }

                cost2.setOrgId(SecurityUtils.getUser().getOrgId());
                if ("1".equals(bean.getIsFlag())) {
                    cost2.setEditTime(LocalDateTime.now());
                    cost2.setEditorId(SecurityUtils.getUser().getId());
                    cost2.setEditorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
                }

                if (cost2.getPaymentId() == null || "".equals(cost.getPaymentId())) {
                    scCostMapper.insert(cost2);
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean doDelete(AfIncome bean) {

        if ("AE".equals(bean.getBusinessScope()) || "AI".equals(bean.getBusinessScope())) {
            baseMapper.deleteById(bean.getIncomeId());
            baseMapper.deleteCostByIncomeId(SecurityUtils.getUser().getOrgId(), bean.getIncomeId());
        } else if ("SE".equals(bean.getBusinessScope()) || "SI".equals(bean.getBusinessScope())) {
            scIncomeMapper.deleteById(bean.getIncomeId());
            baseMapper.deleteCostByIncomeIdSE(SecurityUtils.getUser().getOrgId(), bean.getIncomeId());
        }else if(bean.getBusinessScope().startsWith("T")) {
        	 tcIncomeMapper.deleteById(bean.getIncomeId());
             baseMapper.deleteCostByIncomeIdTC(SecurityUtils.getUser().getOrgId(), bean.getIncomeId());
        }
        return true;
    }

    @Override
    public List<AfCost> queryByIncomeId(Integer id) {

        List<AfCost> costs = baseMapper.queryByIncomeId(SecurityUtils.getUser().getOrgId(), id);
        return costs;
    }

    @Override
    public List<AfCost> queryByIncomeIdSE(Integer id) {

        List<AfCost> costs = baseMapper.queryByIncomeIdSE(SecurityUtils.getUser().getOrgId(), id);
        return costs;
    }

    @Override
    public IPage getPage(Page page, AfIncome income) {
        IPage result = null;
        if (income.getBusinessScope().startsWith("A")) {
            result = getPageForAF(page, income);
        } else if (income.getBusinessScope().startsWith("S")) {
            result = getPageForSC(page, income);
        }else if(income.getBusinessScope().startsWith("T")){
        	result = getPageForTC(page, income);
        }else if(income.getBusinessScope().startsWith("L")) {
        	result = getPageForLC(page, income);
        }else if("IO".equals(income.getBusinessScope())){
            result = getPageForIO(page, income);
        }
        return result;
    }

    @Override
    public void exportExcel(AfIncome income) {
        List<AfIncome> list = null;
        if (income.getBusinessScope().startsWith("A")) {
            list = list(wrapperForAF(income));
            resultForAF(list);
        } else if (income.getBusinessScope().startsWith("S")) {
            List<ScIncome> incomeList = scIncomeMapper.selectList(wrapperForSC(income));
            list = resultForSC(incomeList);
        }else if(income.getBusinessScope().startsWith("T")) {
        	List<TcIncome> incomeList = tcIncomeMapper.selectList(wrapperForTC(income));
            list = this.resultForTC(incomeList);
        }else if(income.getBusinessScope().startsWith("L")) {
        	List<LcIncome> incomeList = lcIncomeMapper.selectList(wrapperForLC(income));
            list = this.resultForLC(incomeList);
        }else if("IO".equals(income.getBusinessScope())){
            List<IoIncome> incomeList = ioIncomeMapper.selectList(wrapperForIO(income));
            list = this.resultForIO(incomeList);
        }
        List<IncomeExcel> result = list.stream().map(afIncome -> {
            IncomeExcel incomeExcel = new IncomeExcel();
            BeanUtils.copyProperties(afIncome, incomeExcel);
            if(!StringUtils.isEmpty(incomeExcel.getSalesName())) {
            	if(incomeExcel.getSalesName().contains(" ")) {
            		incomeExcel.setSalesName(incomeExcel.getSalesName().split(" ")[0]);
            	}
            }
            if(!StringUtils.isEmpty(incomeExcel.getServicerName())) {
            	if(incomeExcel.getServicerName().contains(" ")) {
            		incomeExcel.setServicerName(incomeExcel.getServicerName().split(" ")[0]);
            	}
            }
            return incomeExcel;
        }).sorted((e1,e2)->{
            if(e1.getFlightDate()==null||e2.getFlightDate()==null){
                return 1;
            }else{
               return e2.getFlightDate().compareTo(e1.getFlightDate());
            }
        }).collect(Collectors.toList());

        //自定义字段
        if(!StringUtils.isEmpty(income.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();

            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(income.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if(jsonArr!=null&&jsonArr.size()>0) {
                for(int i=0;i<jsonArr.size();i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    if(("AE".equals(income.getBusinessScope()) || "SE".equals(income.getBusinessScope())) && "flightDate".equals(job.getString("prop"))){
                        headers[i] = "离港日期";
                    }else if(income.getBusinessScope().endsWith("I") && "flightDate".equals(job.getString("prop"))){
                        headers[i] = "到港日期";
                    }else if(income.getBusinessScope().startsWith("T") && "flightDate".equals(job.getString("prop"))){
                        headers[i] = "发车日期";
                    }else if(income.getBusinessScope().endsWith("C") && "flightDate".equals(job.getString("prop"))){
                        headers[i] = "用车日期";
                    }else {
                        headers[i] = job.getString("label");
                    }
                    if(income.getBusinessScope().endsWith("C")&&"awbNumber".equals(job.getString("prop"))) {
                    	headers[i] = "客户单号";
                    }
                    if("IO".equals(income.getBusinessScope())){
                        if("flightDate".equals(job.get("prop"))){
                            headers[i] = "业务日期";
                        }
                        if("awbNumber".equals(job.get("prop"))){
                            headers[i] = "客户单号";
                        }
                    }
                    colunmStrs[i] = job.getString("prop");
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if(result!=null&&result.size()>0) {
                for(IncomeExcel order :result) {
                    LinkedHashMap map = new LinkedHashMap();
                    for(int j=0;j<colunmStrs.length;j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], order));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(WebUtils.getResponse(), "导出EXCEL", headers, listExcel, "Export");

        }else{
            ExportExcel<IncomeExcel> ex = new ExportExcel<IncomeExcel>();
            String flightName = "";
            if("AE".equals(income.getBusinessScope()) || "SE".equals(income.getBusinessScope())){
                flightName="离港日期";
            }else if(income.getBusinessScope().endsWith("I")){
                flightName="到港日期";
            }else if(income.getBusinessScope().endsWith("C")) {
            	flightName="用车日期";
            }else if(income.getBusinessScope().startsWith("T")) {
                flightName="发车日期";
            }else if("IO".equals(income.getBusinessScope())){
                flightName = "业务日期";
            }
            String num = "";
            if(income.getBusinessScope().startsWith("T")) {
            	num = "运单号";
            }else if(income.getBusinessScope().startsWith("A")||income.getBusinessScope().startsWith("S")){
            	num = "主单号";
            }else if(income.getBusinessScope().startsWith("L") || "IO".equals(income.getBusinessScope())){
            	num = "客户单号";
            }
            String[] headers = {"业务范畴",num,"订单号", flightName,"客户名称", "责任销售","责任客服","收款客户","应收费用项目","应收金额"};
            ex.exportExcel(WebUtils.getResponse(), "导出EXCEL", headers, result, "Export");
        }
    }

    private IPage getPageForAF(Page page, AfIncome income) {
        IPage result = baseMapper.getPageForAF(page, wrapperForAF(income), income.getBusinessScope());
        resultForAF(result.getRecords());
        return result;
    }

    private IPage getPageForSC(Page page, AfIncome income) {
//        IPage result = scIncomeMapper.selectPage(page, wrapperForSC(income));
        IPage result = baseMapper.getPageForSC(page, wrapperForSC(income), income.getBusinessScope());
        result.setRecords(resultForSC(result.getRecords()));
        return result;
    }

    private IPage getPageForTC(Page page, AfIncome income) {
      IPage result = baseMapper.getPageForTC(page, wrapperForTC(income), income.getBusinessScope());
      result.setRecords(resultForTC(result.getRecords()));
      return result;
    }

    private IPage getPageForLC(Page page, AfIncome income) {
        IPage result = baseMapper.getPageForLC(page, wrapperForLC(income), income.getBusinessScope());
        result.setRecords(resultForLC(result.getRecords()));
        return result;
    }

    private IPage getPageForIO(Page page, AfIncome afIncome){
        IPage result = baseMapper.getPageForIO(page, wrapperForIO(afIncome));
        result.setRecords(resultForIO(result.getRecords()));
        return result;
    }

    
    private LambdaQueryWrapper<AfIncome> wrapperForAF(AfIncome income) {
        //拼接应收费用查询条件
        LambdaQueryWrapper<AfIncome> wrapper = Wrappers.<AfIncome>lambdaQuery();
        wrapper.eq(AfIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(AfIncome::getBusinessScope, income.getBusinessScope()).isNull(AfIncome::getDebitNoteId);

        //拼接订单查询条件
        if (income.getFlightDateStart() != null || income.getFlightDateEnd() != null || StrUtil.isNotBlank(income.getOrderCode()) || StrUtil.isNotBlank(income.getAwbNumber())) {
            LambdaQueryWrapper<AfOrder> afOrderWrapper = Wrappers.<AfOrder>lambdaQuery();
            afOrderWrapper.eq(AfOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            if (income.getFlightDateStart() != null) {
                if (income.getBusinessScope().equals("AE")) {
                    afOrderWrapper.ge(AfOrder::getExpectDeparture, income.getFlightDateStart());
                } else if (income.getBusinessScope().equals("AI")) {
                    afOrderWrapper.ge(AfOrder::getExpectArrival, income.getFlightDateStart());
                }
            }
            if (income.getFlightDateEnd() != null) {
                if (income.getBusinessScope().equals("AE")) {
                    afOrderWrapper.le(AfOrder::getExpectDeparture, income.getFlightDateEnd());
                } else if (income.getBusinessScope().equals("AI")) {
                    afOrderWrapper.le(AfOrder::getExpectArrival, income.getFlightDateEnd());
                }
            }
            if (StrUtil.isNotBlank(income.getOrderCode())) {
                afOrderWrapper.like(AfOrder::getOrderCode, "%" + income.getOrderCode() + "%");
            }
            if (StrUtil.isNotBlank(income.getAwbNumber())) {
                if (income.getBusinessScope().equals("AE")) {
                    afOrderWrapper.like(AfOrder::getAwbNumber, "%" + income.getAwbNumber() + "%");
                } else if (income.getBusinessScope().equals("AI")) {
                    afOrderWrapper.and(i -> i.like(AfOrder::getAwbNumber, "%" + income.getAwbNumber() + "%").or(j -> j.like(AfOrder::getHawbNumber, "%" + income.getAwbNumber() + "%")));
                }
            }
            List<Integer> orderIdList = afOrderService.list(afOrderWrapper).stream().map(afOrder -> afOrder.getOrderId()).collect(Collectors.toList());
            if (orderIdList.size()==0) {
            	orderIdList.add(-1);
			}
            if (orderIdList != null && orderIdList.size() != 0) {
                wrapper.in(AfIncome::getOrderId, orderIdList);
            }
        }
        return wrapper;
    }

    
    private LambdaQueryWrapper<TcIncome> wrapperForTC(AfIncome income) {
        //拼接应收费用查询条件
        LambdaQueryWrapper<TcIncome> wrapper = Wrappers.<TcIncome>lambdaQuery();
        wrapper.eq(TcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(TcIncome::getBusinessScope, income.getBusinessScope()).isNull(TcIncome::getDebitNoteId);

        //拼接订单查询条件
        if (income.getFlightDateStart() != null || income.getFlightDateEnd() != null || StrUtil.isNotBlank(income.getOrderCode()) || StrUtil.isNotBlank(income.getAwbNumber())) {
            LambdaQueryWrapper<TcOrder> tcOrderWrapper = Wrappers.<TcOrder>lambdaQuery();
            tcOrderWrapper.eq(TcOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            if (income.getFlightDateStart() != null) {
                if (income.getBusinessScope().equals("TE")) {
                	tcOrderWrapper.ge(TcOrder::getExpectDeparture, income.getFlightDateStart());
                } else if (income.getBusinessScope().equals("TI")) {
                	tcOrderWrapper.ge(TcOrder::getExpectArrival, income.getFlightDateStart());
                }
            }
            if (income.getFlightDateEnd() != null) {
                if (income.getBusinessScope().equals("TE")) {
                	tcOrderWrapper.le(TcOrder::getExpectDeparture, income.getFlightDateEnd());
                } else if (income.getBusinessScope().equals("TI")) {
                	tcOrderWrapper.le(TcOrder::getExpectArrival, income.getFlightDateEnd());
                }
            }
            if (StrUtil.isNotBlank(income.getOrderCode())) {
            	tcOrderWrapper.like(TcOrder::getOrderCode,income.getOrderCode());
            }
            if (StrUtil.isNotBlank(income.getAwbNumber())) {
               tcOrderWrapper.like(TcOrder::getRwbNumber,income.getAwbNumber());
            }
            List<Integer> orderIdList = tcOrderService.list(tcOrderWrapper).stream().map(afOrder -> afOrder.getOrderId()).collect(Collectors.toList());
            if (orderIdList.size()==0) {
            	orderIdList.add(-1);
			}
            if (orderIdList != null && orderIdList.size() != 0) {
                wrapper.in(TcIncome::getOrderId, orderIdList);
            }
        }
        return wrapper;
    }
    
    private LambdaQueryWrapper<LcIncome> wrapperForLC(AfIncome income) {
        //拼接应收费用查询条件
        LambdaQueryWrapper<LcIncome> wrapper = Wrappers.<LcIncome>lambdaQuery();
        wrapper.eq(LcIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcIncome::getBusinessScope, income.getBusinessScope()).isNull(LcIncome::getDebitNoteId);

        //拼接订单查询条件
        if (income.getFlightDateStart() != null || income.getFlightDateEnd() != null || StrUtil.isNotBlank(income.getOrderCode()) || StrUtil.isNotBlank(income.getAwbNumber())) {
            LambdaQueryWrapper<LcOrder> lcOrderWrapper = Wrappers.<LcOrder>lambdaQuery();
            lcOrderWrapper.eq(LcOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            if (income.getFlightDateStart() != null) {
            	lcOrderWrapper.ge(LcOrder::getDrivingTime, income.getFlightDateStart());
            }
            if (income.getFlightDateEnd() != null) {
            	lcOrderWrapper.le(LcOrder::getDrivingTime, income.getFlightDateEnd());
            }
            if (StrUtil.isNotBlank(income.getOrderCode())) {
            	lcOrderWrapper.like(LcOrder::getOrderCode,income.getOrderCode());
            }
            if (StrUtil.isNotBlank(income.getAwbNumber())) {
               lcOrderWrapper.like(LcOrder::getCustomerNumber,income.getAwbNumber());
            }
            List<Integer> orderIdList = lcOrderService.list(lcOrderWrapper).stream().map(lcOrder -> lcOrder.getOrderId()).collect(Collectors.toList());
            if (orderIdList.size()==0) {
            	orderIdList.add(-1);
			}
            if (orderIdList != null && orderIdList.size() != 0) {
                wrapper.in(LcIncome::getOrderId, orderIdList);
            }
        }
        return wrapper;
    }

    private LambdaQueryWrapper<IoIncome> wrapperForIO(AfIncome income) {
        //拼接应收费用查询条件
        LambdaQueryWrapper<IoIncome> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(IoIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(IoIncome::getBusinessScope, income.getBusinessScope()).isNull(IoIncome::getDebitNoteId);

        //拼接订单查询条件
        if (income.getFlightDateStart() != null || income.getFlightDateEnd() != null || StrUtil.isNotBlank(income.getOrderCode()) || StrUtil.isNotBlank(income.getAwbNumber())) {
            LambdaQueryWrapper<IoOrder> orderWrapper = Wrappers.lambdaQuery();
            orderWrapper.eq(IoOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            if (income.getFlightDateStart() != null) {
                orderWrapper.ge(IoOrder::getBusinessDate, income.getFlightDateStart());
            }
            if (income.getFlightDateEnd() != null) {
                orderWrapper.le(IoOrder::getBusinessDate, income.getFlightDateEnd());
            }
            if (StrUtil.isNotBlank(income.getOrderCode())) {
                orderWrapper.like(IoOrder::getOrderCode,income.getOrderCode());
            }
            if (StrUtil.isNotBlank(income.getAwbNumber())) {
                orderWrapper.like(IoOrder::getCustomerNumber,income.getAwbNumber());
            }
            List<Integer> orderIdList = ioOrderService.list(orderWrapper).stream().map(order -> order.getOrderId()).collect(Collectors.toList());
            if (orderIdList.size()==0) {
                orderIdList.add(-1);
            }
            if (orderIdList != null && orderIdList.size() != 0) {
                wrapper.in(IoIncome::getOrderId, orderIdList);
            }
        }
        return wrapper;
    }

    private LambdaQueryWrapper<ScIncome> wrapperForSC(AfIncome income) {
        //拼接应收费用查询条件
        LambdaQueryWrapper<ScIncome> wrapper = Wrappers.<ScIncome>lambdaQuery();
        wrapper.eq(ScIncome::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScIncome::getBusinessScope, income.getBusinessScope()).isNull(ScIncome::getDebitNoteId);

        //拼接订单查询条件
        if (income.getFlightDateStart() != null || income.getFlightDateEnd() != null || StrUtil.isNotBlank(income.getOrderCode()) || StrUtil.isNotBlank(income.getAwbNumber())) {
            LambdaQueryWrapper<ScOrder> scOrderWrapper = Wrappers.<ScOrder>lambdaQuery();
            scOrderWrapper.eq(ScOrder::getOrgId, SecurityUtils.getUser().getOrgId());
            if (income.getFlightDateStart() != null) {
                if (income.getBusinessScope().equals("SE")) {
                    scOrderWrapper.ge(ScOrder::getExpectDeparture, income.getFlightDateStart());
                } else if (income.getBusinessScope().equals("SI")) {
                    scOrderWrapper.ge(ScOrder::getExpectArrival, income.getFlightDateStart());
                }
            }
            if (income.getFlightDateEnd() != null) {
                if (income.getBusinessScope().equals("SE")) {
                    scOrderWrapper.le(ScOrder::getExpectDeparture, income.getFlightDateEnd());
                } else if (income.getBusinessScope().equals("SI")) {
                    scOrderWrapper.le(ScOrder::getExpectArrival, income.getFlightDateEnd());
                }
            }
            if (StrUtil.isNotBlank(income.getOrderCode())) {
                scOrderWrapper.like(ScOrder::getOrderCode, "%" + income.getOrderCode() + "%");
            }
            if (StrUtil.isNotBlank(income.getAwbNumber())) {
                if (income.getBusinessScope().equals("SE")) {
                    scOrderWrapper.like(ScOrder::getMblNumber, "%" + income.getAwbNumber() + "%");
                } else if (income.getBusinessScope().equals("SI")) {
                    scOrderWrapper.and(i -> i.like(ScOrder::getMblNumber, "%" + income.getAwbNumber() + "%").or(j -> j.like(ScOrder::getHblNumber, "%" + income.getAwbNumber() + "%")));
                }
            }
            List<Integer> orderIdList = scOrderService.list(scOrderWrapper).stream().map(afOrder -> afOrder.getOrderId()).collect(Collectors.toList());
            if (orderIdList.size()==0) {
            	orderIdList.add(-1);
			}
            if (orderIdList != null && orderIdList.size() != 0) {
                wrapper.in(ScIncome::getOrderId, orderIdList);
            }
        }
        return wrapper;
    }

    private void resultForAF(List<AfIncome> records) {
        records.stream().forEach(income -> {
            AfOrder order = afOrderService.getById(income.getOrderId());
            if (order != null) {
                if (income.getBusinessScope().equals("AE")) {
                    income.setAwbNumber(order.getAwbNumber());
                    income.setFlightDate(order.getExpectDeparture());
                } else if (income.getBusinessScope().equals("AI")) {
                    if (StrUtil.isNotBlank(order.getAwbNumber()) && StrUtil.isNotBlank(order.getHawbNumber())) {
                        income.setAwbNumber(order.getAwbNumber() + "_" + order.getHawbNumber());
                    } else if (StrUtil.isNotBlank(order.getAwbNumber())) {
                        income.setAwbNumber(order.getAwbNumber());
                    } else if (StrUtil.isNotBlank(order.getHawbNumber())) {
                        income.setAwbNumber(order.getHawbNumber());
                    }
                    income.setFlightDate(order.getExpectArrival());
                }
                income.setOrderCode(order.getOrderCode());
                CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo != null) {
                    income.setOrderCustomerName(coopVo.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if(!StrUtil.isEmpty(salesName)){
                	income.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if(!StrUtil.isEmpty(servicerName)){
                	income.setServicerName(servicerName.split(" ")[0]);
                }
                income.setServicerName(order.getServicerName());
                income.setSalesName(order.getSalesName());
                income.setIncomeAmountStr(FormatUtils.formatWithQWF(income.getIncomeAmount(), 2) + " (" + income.getIncomeCurrency() + ")");
            }
        });
    }


    private List<AfIncome> resultForSC(List<ScIncome> records) {
        List<AfIncome> afIncomeList = records.stream().map(scIncome -> {
            AfIncome income = new AfIncome();
            BeanUtils.copyProperties(scIncome, income);
            ScOrder order = scOrderService.getById(income.getOrderId());
            if (order != null) {
                if (income.getBusinessScope().equals("SE")) {
                    income.setFlightDate(order.getExpectDeparture());
                } else if (income.getBusinessScope().equals("SI")) {
                    income.setFlightDate(order.getExpectArrival());
                }
                if (StrUtil.isNotBlank(order.getMblNumber()) && StrUtil.isNotBlank(order.getHblNumber())) {
                    income.setAwbNumber(order.getMblNumber() + "_" + order.getHblNumber());
                } else if (StrUtil.isNotBlank(order.getMblNumber())) {
                    income.setAwbNumber(order.getMblNumber());
                } else if (StrUtil.isNotBlank(order.getHblNumber())) {
                    income.setAwbNumber(order.getHblNumber());
                }
                income.setOrderCode(order.getOrderCode());
                CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo != null) {
                    income.setOrderCustomerName(coopVo.getCoop_name());
                }
              //责任销售
                String salesName = order.getSalesName();
                if(!StrUtil.isEmpty(salesName)){
                	income.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if(!StrUtil.isEmpty(servicerName)){
                	income.setServicerName(servicerName.split(" ")[0]);
                }
                income.setServicerName(order.getServicerName());
                income.setSalesName(order.getSalesName());
                income.setIncomeAmountStr(FormatUtils.formatWithQWF(income.getIncomeAmount(), 2) + " (" + income.getIncomeCurrency() + ")");
            }
            return income;
        }).collect(Collectors.toList());
        return afIncomeList;
    }
    
    private List<AfIncome> resultForTC(List<TcIncome> records) {
        List<AfIncome> afIncomeList = records.stream().map(tcIncome -> {
            AfIncome income = new AfIncome();
            BeanUtils.copyProperties(tcIncome, income);
            TcOrder order = tcOrderService.getById(income.getOrderId());
            if (order != null) {
                if (income.getBusinessScope().equals("TE")) {
                    income.setFlightDate(order.getExpectDeparture());
                } else if (income.getBusinessScope().equals("TI")) {
                    income.setFlightDate(order.getExpectArrival());
                }
                if (StrUtil.isNotBlank(order.getRwbNumber())) {
                    income.setAwbNumber(order.getRwbNumber());
                }
                income.setOrderCode(order.getOrderCode());
                CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo != null) {
                    income.setOrderCustomerName(coopVo.getCoop_name());
                }
              //责任销售
                String salesName = order.getSalesName();
                if(!StrUtil.isEmpty(salesName)){
                	income.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if(!StrUtil.isEmpty(servicerName)){
                	income.setServicerName(servicerName.split(" ")[0]);
                }
                income.setServicerName(order.getServicerName());
                income.setSalesName(order.getSalesName());
                income.setIncomeAmountStr(FormatUtils.formatWithQWF(income.getIncomeAmount(), 2) + " (" + income.getIncomeCurrency() + ")");
            }
            return income;
        }).collect(Collectors.toList());
        return afIncomeList;
    }

    private List<AfIncome> resultForLC(List<LcIncome> records) {
        List<AfIncome> afIncomeList = records.stream().map(lcIncome -> {
            AfIncome income = new AfIncome();
            BeanUtils.copyProperties(lcIncome, income);
            LcOrder order = lcOrderService.getById(income.getOrderId());
            if (order != null) {
            	income.setFlightDate(order.getDrivingTime().toLocalDate());
                if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                    income.setAwbNumber(order.getCustomerNumber());
                }
                income.setOrderCode(order.getOrderCode());
                CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo != null) {
                    income.setOrderCustomerName(coopVo.getCoop_name());
                }
              //责任销售
                String salesName = order.getSalesName();
                if(!StrUtil.isEmpty(salesName)){
                	income.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if(!StrUtil.isEmpty(servicerName)){
                	income.setServicerName(servicerName.split(" ")[0]);
                }
                income.setServicerName(order.getServicerName());
                income.setSalesName(order.getSalesName());
                income.setIncomeAmountStr(FormatUtils.formatWithQWF(income.getIncomeAmount(), 2) + " (" + income.getIncomeCurrency() + ")");
            }
            return income;
        }).collect(Collectors.toList());
        return afIncomeList;
    }

    private List<AfIncome> resultForIO(List<IoIncome> records) {
        List<AfIncome> afIncomeList = records.stream().map(ioIncome -> {
            AfIncome income = new AfIncome();
            BeanUtils.copyProperties(ioIncome, income);
            IoOrder order = ioOrderService.getById(income.getOrderId());
            if (order != null) {
                income.setFlightDate(order.getBusinessDate());
                if (StrUtil.isNotBlank(order.getCustomerNumber())) {
                    income.setAwbNumber(order.getCustomerNumber());
                }
                income.setOrderCode(order.getOrderCode());
                CoopVo coopVo = remoteCoopService.viewCoop(order.getCoopId().toString()).getData();
                if (coopVo != null) {
                    income.setOrderCustomerName(coopVo.getCoop_name());
                }
                //责任销售
                String salesName = order.getSalesName();
                if(!StrUtil.isEmpty(salesName)){
                    income.setSalesName(salesName.split(" ")[0]);
                }
                //责任客服
                String servicerName = order.getServicerName();
                if(!StrUtil.isEmpty(servicerName)){
                    income.setServicerName(servicerName.split(" ")[0]);
                }
                income.setServicerName(order.getServicerName());
                income.setSalesName(order.getSalesName());
                income.setIncomeAmountStr(FormatUtils.formatWithQWF(income.getIncomeAmount(), 2) + " (" + income.getIncomeCurrency() + ")");
            }
            return income;
        }).collect(Collectors.toList());
        return afIncomeList;
    }
	@Override
	public List<AfCost> queryByIncomeIdTC(Integer id) {
		List<AfCost> costs = baseMapper.queryByIncomeIdTC(SecurityUtils.getUser().getOrgId(), id);
        return costs;
	}
	
}

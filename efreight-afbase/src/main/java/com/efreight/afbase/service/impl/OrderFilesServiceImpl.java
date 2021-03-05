package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.OrderFiles;
import com.efreight.afbase.entity.ScOrderFiles;
import com.efreight.afbase.entity.TcOrderFiles;
import com.efreight.afbase.dao.OrderFilesMapper;
import com.efreight.afbase.dao.ScOrderFilesMapper;
import com.efreight.afbase.dao.TcOrderFilesMapper;
import com.efreight.afbase.service.LogService;
import com.efreight.afbase.service.OrderFilesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.core.feign.RemoteServiceToSC;
import com.efreight.common.remoteVo.IoOrderFiles;
import com.efreight.common.remoteVo.LcOrderFiles;
import com.efreight.common.remoteVo.VlOrderFiles;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;

import lombok.AllArgsConstructor;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * AF 订单管理 出口订单附件 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-02-12
 */
@Service
@AllArgsConstructor
public class OrderFilesServiceImpl extends ServiceImpl<OrderFilesMapper, OrderFiles> implements OrderFilesService {

    private final ScOrderFilesMapper scOrderFilesMapper;
    private final TcOrderFilesMapper tcOrderFilesMapper;
    private final RemoteServiceToSC remoteServiceToSC;
    private final LogService logService;

    @Override
    public IPage getPage(Page page, OrderFiles orderFiles) {
        LambdaQueryWrapper<OrderFiles> wrapper = Wrappers.<OrderFiles>lambdaQuery();
        wrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId());
        return page(page, wrapper);
    }

    @Override
    public void insert(OrderFiles orderFiles) {
        if (orderFiles.getOrderId() == null) {
            throw new RuntimeException("订单信息不能为空");
        }

        orderFiles.setCreateTime(LocalDateTime.now());
        orderFiles.setCreatorId(SecurityUtils.getUser().getId());
        orderFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderFiles.setOrgId(SecurityUtils.getUser().getOrgId());

        String businessScope = orderFiles.getBusinessScope();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            LambdaQueryWrapper<OrderFiles> orderWrapper = Wrappers.<OrderFiles>lambdaQuery();
            orderWrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderFiles::getOrderId, orderFiles.getOrderId());
            List<OrderFiles> list = list(orderWrapper);
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            save(orderFiles);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            List<OrderFiles> list = baseMapper.getSCList(SecurityUtils.getUser().getOrgId(), orderFiles.getOrderId());
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            ScOrderFiles scOrderFiles = new ScOrderFiles();
            BeanUtils.copyProperties(orderFiles, scOrderFiles);
            scOrderFilesMapper.insert(scOrderFiles);
        } else if ("TE".equals(businessScope) || "TI".equals(businessScope)) {
            List<OrderFiles> list = baseMapper.getTCList(SecurityUtils.getUser().getOrgId(), orderFiles.getOrderId());
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            TcOrderFiles tcOrderFiles = new TcOrderFiles();
            BeanUtils.copyProperties(orderFiles, tcOrderFiles);
            tcOrderFilesMapper.insert(tcOrderFiles);
        } else if ("LC".equals(businessScope)) {
            List<LcOrderFiles> list = remoteServiceToSC.listLcOrderFiles(orderFiles.getOrderId()).getData();
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            LcOrderFiles lcOrderFiles = new LcOrderFiles();
            BeanUtils.copyProperties(orderFiles, lcOrderFiles);
            MessageInfo messageInfo = remoteServiceToSC.saveLcOrderFiles(lcOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("IO".equals(businessScope)) {
            List<IoOrderFiles> list = remoteServiceToSC.listIoOrderFiles(orderFiles.getOrderId()).getData();
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            IoOrderFiles ioOrderFiles = new IoOrderFiles();
            BeanUtils.copyProperties(orderFiles, ioOrderFiles);
            MessageInfo messageInfo = remoteServiceToSC.saveIoOrderFiles(ioOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("VL".equals(businessScope)) {
            List<VlOrderFiles> list = remoteServiceToSC.listVLOrderFiles(orderFiles.getOrderId()).getData();
            if (list.size() == 99 || list.size() > 99) {
                throw new RuntimeException("订单附件已达上限,无法新增");
            }
            VlOrderFiles vlOrderFiles = new VlOrderFiles();
            BeanUtils.copyProperties(orderFiles, vlOrderFiles);
            MessageInfo messageInfo = remoteServiceToSC.saveVLOrderFiles(vlOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }
      //添加日志
//  		LogBean logBean = new LogBean();
//  		logBean.setPageName(orderFiles.getPageName());
//  		logBean.setPageFunction("上传电子单证");
//  		logBean.setBusinessScope(businessScope);
//  		
//  		logBean.setOrderNumber(orderFiles.getOrderCode());
//  		logBean.setLogRemark("附件名称："+orderFiles.getFileName());
//  		logBean.setOrderId(orderFiles.getOrderId());
//  		logBean.setOrderUuid(orderFiles.getOrderUuid());
//  		logService.saveLog(logBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatch(OrderFiles orderFiles) {
        if (orderFiles.getOrderId() == null) {
            throw new RuntimeException("订单信息不能为空");
        }

        orderFiles.setCreateTime(LocalDateTime.now());
        orderFiles.setCreatorId(SecurityUtils.getUser().getId());
        orderFiles.setCreatorName(SecurityUtils.getUser().getUserCname() + " " + SecurityUtils.getUser().getUserEmail());
        orderFiles.setOrgId(SecurityUtils.getUser().getOrgId());

        String businessScope = orderFiles.getBusinessScope();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    LambdaQueryWrapper<OrderFiles> orderWrapper = Wrappers.<OrderFiles>lambdaQuery();
                    orderWrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderFiles::getOrderId, orderFiles.getOrderId());
                    List<OrderFiles> list = list(orderWrapper);
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    save(orderFiles);
                }
            }
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    List<OrderFiles> list = baseMapper.getSCList(SecurityUtils.getUser().getOrgId(), orderFiles.getOrderId());
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    ScOrderFiles scOrderFiles = new ScOrderFiles();
                    BeanUtils.copyProperties(orderFiles, scOrderFiles);
                    scOrderFilesMapper.insert(scOrderFiles);
                }
            }
        } else if ("TE".equals(businessScope) || "TI".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    List<OrderFiles> list = baseMapper.getTCList(SecurityUtils.getUser().getOrgId(), orderFiles.getOrderId());
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    TcOrderFiles tcOrderFiles = new TcOrderFiles();
                    BeanUtils.copyProperties(orderFiles, tcOrderFiles);
                    tcOrderFilesMapper.insert(tcOrderFiles);
                }
            }
        } else if ("LC".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    List<LcOrderFiles> list = remoteServiceToSC.listLcOrderFiles(orderFiles.getOrderId()).getData();
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    LcOrderFiles lcOrderFiles = new LcOrderFiles();
                    BeanUtils.copyProperties(orderFiles, lcOrderFiles);
                    MessageInfo messageInfo = remoteServiceToSC.saveLcOrderFiles(lcOrderFiles);
                    if (messageInfo.getCode() == 1) {
                        throw new RuntimeException(messageInfo.getMessageInfo());
                    }
                }
            }
        } else if ("IO".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    List<IoOrderFiles> list = remoteServiceToSC.listIoOrderFiles(orderFiles.getOrderId()).getData();
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    IoOrderFiles ioOrderFiles = new IoOrderFiles();
                    BeanUtils.copyProperties(orderFiles, ioOrderFiles);
                    MessageInfo messageInfo = remoteServiceToSC.saveIoOrderFiles(ioOrderFiles);
                    if (messageInfo.getCode() == 1) {
                        throw new RuntimeException(messageInfo.getMessageInfo());
                    }
                }
            }
        } else if ("VL".equals(businessScope)) {
            if (orderFiles.getFileLists() != null && orderFiles.getFileLists().size() > 0) {
                for (int i = 0; i < orderFiles.getFileLists().size(); i++) {
                    List<VlOrderFiles> list = remoteServiceToSC.listVLOrderFiles(orderFiles.getOrderId()).getData();
                    if (list.size() == 99 || list.size() > 99) {
                        throw new RuntimeException("订单附件已达上限,无法新增");
                    }
                    orderFiles.setFileName(orderFiles.getFileLists().get(i).getName());
                    orderFiles.setFileUrl(orderFiles.getFileLists().get(i).getUrl());
                    VlOrderFiles vlOrderFiles = new VlOrderFiles();
                    BeanUtils.copyProperties(orderFiles, vlOrderFiles);
                    MessageInfo messageInfo = remoteServiceToSC.saveVLOrderFiles(vlOrderFiles);
                    if (messageInfo.getCode() == 1) {
                        throw new RuntimeException(messageInfo.getMessageInfo());
                    }
                }
            }
        }
      //添加日志
//  		LogBean logBean = new LogBean();
//  		logBean.setPageName(orderFiles.getPageName());
//  		logBean.setPageFunction("批量上传");
//  		logBean.setBusinessScope(businessScope);
//  		
//  		logBean.setOrderNumber(orderFiles.getOrderCode());
//  		logBean.setLogRemark("总共上传"+orderFiles.getFileLists().size()+"个电子单证");
//  		logBean.setOrderId(orderFiles.getOrderId());
//  		logBean.setOrderUuid(orderFiles.getOrderUuid());
//  		logService.saveLog(logBean);
    }

    @Override
    public void delete(OrderFiles bean) {
    	Integer orderFilesId=bean.getOrderFileId();
    	String businessScope=bean.getBusinessScope();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            removeById(orderFilesId);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            scOrderFilesMapper.deleteById(orderFilesId);
        } else if ("TE".equals(businessScope) || "TI".equals(businessScope)) {
            tcOrderFilesMapper.deleteById(orderFilesId);
        } else if ("LC".equals(businessScope)) {
            remoteServiceToSC.deleteLcOrderFiles(orderFilesId);
        } else if ("IO".equals(businessScope)) {
            remoteServiceToSC.deleteIoOrderFiles(orderFilesId);
        } else if ("VL".equals(businessScope)) {
            MessageInfo messageInfo = remoteServiceToSC.deleteVLOrderFiles(orderFilesId);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }
        
      //添加日志
//  		LogBean logBean = new LogBean();
//  		logBean.setPageName(bean.getPageName());
//  		logBean.setPageFunction("删除电子单证");
//  		logBean.setBusinessScope(businessScope);
//  		
//  		logBean.setOrderNumber(bean.getOrderCode());
//  		logBean.setLogRemark("附件名称："+bean.getFileName());
//  		logBean.setOrderId(bean.getOrderId());
//  		logBean.setOrderUuid(bean.getOrderUuid());
//  		logService.saveLog(logBean);
    }

    @Override
    public void showFile(OrderFiles bean) {
    	Integer orderFilesId=bean.getOrderFileId();
    	Integer isDisplay=bean.getIsDisplay();
    	String businessScope=bean.getBusinessScope();
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            baseMapper.upDateShowFile(orderFilesId, isDisplay);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            baseMapper.upDateShowFileSc(orderFilesId, isDisplay);
        } else if ("TE".equals(businessScope) || "TI".equals(businessScope)) {
            baseMapper.upDateShowFileTC(orderFilesId, isDisplay);
        } else if ("LC".equals(businessScope)) {
            LcOrderFiles lcOrderFiles = new LcOrderFiles();
            lcOrderFiles.setOrderFileId(orderFilesId);
            lcOrderFiles.setBusinessScope(businessScope);
            lcOrderFiles.setIsDisplay(isDisplay);
            MessageInfo messageInfo = remoteServiceToSC.modifyLcOrderFiles(lcOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("IO".equals(businessScope)) {
            IoOrderFiles ioOrderFiles = new IoOrderFiles();
            ioOrderFiles.setOrderFileId(orderFilesId);
            ioOrderFiles.setBusinessScope(businessScope);
            ioOrderFiles.setIsDisplay(isDisplay);
            MessageInfo messageInfo = remoteServiceToSC.modifyIoOrderFiles(ioOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        } else if ("VL".equals(businessScope)) {
            VlOrderFiles vlOrderFiles = new VlOrderFiles();
            vlOrderFiles.setOrderFileId(orderFilesId);
            vlOrderFiles.setBusinessScope(businessScope);
            vlOrderFiles.setIsDisplay(isDisplay);
            MessageInfo messageInfo = remoteServiceToSC.modifyVLOrderFiles(vlOrderFiles);
            if (messageInfo.getCode() == 1) {
                throw new RuntimeException(messageInfo.getMessageInfo());
            }
        }
      //添加日志
//  		LogBean logBean = new LogBean();
//  		logBean.setPageName(bean.getPageName());
//  		String function="";
//  		if (isDisplay==1) {
//  			function="显示";
//		} else {
//			function="不显示";
//		}
//  		logBean.setPageFunction(function);
//  		logBean.setBusinessScope(businessScope);
//  		
//  		logBean.setOrderNumber(bean.getOrderCode());
//  		logBean.setLogRemark("附件名称："+bean.getFileName());
//  		logBean.setOrderId(bean.getOrderId());
//  		logBean.setOrderUuid(bean.getOrderUuid());
//  		logService.saveLog(logBean);
    }

    @Override
    public void insertBatchForAF(List<OrderFiles> orderFilesList) {
        if (orderFilesList.size() == 0) {
            throw new RuntimeException("请上传附件");
        }
        LambdaQueryWrapper<OrderFiles> orderWrapper = Wrappers.<OrderFiles>lambdaQuery();
        orderWrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderFiles::getOrderId, orderFilesList.get(0).getOrderId());
        List<OrderFiles> list = list(orderWrapper);
        if (list.size() == 99 || list.size() > 99) {
            throw new RuntimeException("订单附件已达上限,无法新增");
        }
        orderFilesList.stream().forEach(orderFiles -> {
            orderFiles.setCreateTime(LocalDateTime.now());
            orderFiles.setCreatorId(SecurityUtils.getUser().getId());
            orderFiles.setCreatorName(SecurityUtils.getUser().buildOptName());
            orderFiles.setOrgId(SecurityUtils.getUser().getOrgId());
        });
        saveBatch(orderFilesList);
    }

    @Override
    public List<OrderFiles> getList(Integer orderId, String businessScope) {
        if ("AE".equals(businessScope) || "AI".equals(businessScope)) {
            LambdaQueryWrapper<OrderFiles> wrapper = Wrappers.<OrderFiles>lambdaQuery();
            wrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderFiles::getOrderId, orderId);
            return list(wrapper);
        } else if ("SE".equals(businessScope) || "SI".equals(businessScope)) {
            LambdaQueryWrapper<ScOrderFiles> wrapper = Wrappers.<ScOrderFiles>lambdaQuery();
            wrapper.eq(ScOrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(ScOrderFiles::getOrderId, orderId);
            return baseMapper.getSCList(SecurityUtils.getUser().getOrgId(), orderId);
        } else if ("LC".equals(businessScope)) {
            return remoteServiceToSC.listLcOrderFiles(orderId).getData().stream().map(lcOrderFile -> {
                OrderFiles orderFiles = new OrderFiles();
                BeanUtils.copyProperties(lcOrderFile, orderFiles);
                return orderFiles;
            }).collect(Collectors.toList());
        } else if ("IO".equals(businessScope)) {
            return remoteServiceToSC.listIoOrderFiles(orderId).getData().stream().map(ioOrderFile -> {
                OrderFiles orderFiles = new OrderFiles();
                BeanUtils.copyProperties(ioOrderFile, orderFiles);
                return orderFiles;
            }).collect(Collectors.toList());
        } else if (businessScope.startsWith("T")) {
            return baseMapper.getTCList(SecurityUtils.getUser().getOrgId(), orderId);
        } else if ("VL".equals(businessScope)) {
            return remoteServiceToSC.listVLOrderFiles(orderId).getData().stream().map(vlOrderFile -> {
                OrderFiles orderFiles = new OrderFiles();
                BeanUtils.copyProperties(vlOrderFile, orderFiles);
                return orderFiles;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<OrderFiles> getListByWhere(OrderFiles bean) {
        if ("AE".equals(bean.getBusinessScope())) {
            LambdaQueryWrapper<OrderFiles> wrapper = Wrappers.<OrderFiles>lambdaQuery();
            wrapper.eq(OrderFiles::getOrgId, SecurityUtils.getUser().getOrgId()).eq(OrderFiles::getOrderId, bean.getOrderId());
            wrapper.eq(OrderFiles::getIsDisplay, 1);
            return list(wrapper);
        }
        return null;
    }
}

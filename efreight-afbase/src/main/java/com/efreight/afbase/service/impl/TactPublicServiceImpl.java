package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.TactPublicMapper;
import com.efreight.afbase.entity.LogBean;
import com.efreight.afbase.entity.TactPublic;
import com.efreight.afbase.service.TactPublicService;
import com.efreight.common.core.feign.RemoteServiceToHRS;
import com.efreight.common.security.constant.SecurityConstants;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.common.security.vo.LogVo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
@AllArgsConstructor
public class TactPublicServiceImpl extends ServiceImpl<TactPublicMapper, TactPublic> implements TactPublicService {

    private final TactPublicMapper tactMapper;
    private final RemoteServiceToHRS remoteServiceToHRS;

    @Override
    public IPage<TactPublic> getListPage(Page page, TactPublic bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.getList(page, bean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTactById(Integer tactId) {
        EUserDetails userDetail = SecurityUtils.getUser();
        TactPublic tact = tactMapper.selectById(tactId);
        tactMapper.deleteById(tactId);
        //HRS日志
        LogVo logVo = new LogVo();
        logVo.setOpType("删除");
        logVo.setOpName("TACT公布价");
        logVo.setOpLevel("低");
        logVo.setOpInfo("公布价产品名称:" + tact.getProductName());
        logVo.setCreatorId(userDetail.getId());
        logVo.setOrgId(userDetail.getOrgId());
        logVo.setDeptId(userDetail.getDeptId());
        remoteServiceToHRS.recordLog(logVo, SecurityConstants.FROM_IN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveTack(TactPublic tact) {
        //添加日志信息
        LogBean logBean = new LogBean();
        Integer tactId = tact.getPublishPriceId();
        //校验参数
        checkTack(tact);
        EUserDetails userDetail = SecurityUtils.getUser();
        if (tactId == null) {
            tact.setCreatorId(userDetail.getId());
            tact.setCreatorName(buildName(userDetail));
            tact.setCreateTime(LocalDateTime.now());
            tact.setOrgId(userDetail.getOrgId());
            tactId = tactMapper.insert(tact);

        }else{
            tact.setEditorId(userDetail.getId());
            tact.setEditorName(buildName(userDetail));
            tact.setEditTime(LocalDateTime.now());
            tactMapper.updateById(tact);
        }

        return tactId;
    }

    private void checkTack(TactPublic tact) {
        //数据验证
        if (StringUtils.isBlank(tact.getProductName())) {
            throw new IllegalArgumentException("请输入产品名称");
        }
        if (StringUtils.isBlank(tact.getDepartureStation())) {
            throw new IllegalArgumentException("请输入始发港");
        }
        if (StringUtils.isBlank(tact.getArrivalStation())) {
            throw new IllegalArgumentException("请输入目的港");
        }
        if (tact.getBeginDate() == null) {
            throw new IllegalArgumentException("请输入生效日期");
        }
        if (tact.getEndDate() == null) {
            throw new IllegalArgumentException("请输入失效日期");
        }
    }

    private String buildName(EUserDetails userDetail) {
        StringBuilder builder = new StringBuilder();
        builder.append(userDetail.getUserCname());
        builder.append(" ");
        builder.append(userDetail.getUserEmail());
        return builder.toString();
    }

    @Override
    public List<TactPublic> queryListForExcel(TactPublic bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        List<TactPublic> pa = baseMapper.queryListForExcel(bean);
        return pa;
    }

}

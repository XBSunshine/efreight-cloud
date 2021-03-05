package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.entity.view.AfVPrmCoop;
import com.efreight.sc.dao.AfVPrmCoopMapper;
import com.efreight.sc.service.AfVPrmCoopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-03-09
 */
@Service
public class AfVPrmCoopServiceImpl extends ServiceImpl<AfVPrmCoopMapper, AfVPrmCoop> implements AfVPrmCoopService {

    @Override
    public List<AfVPrmCoop> getList(AfVPrmCoop afVPrmCoop) {
        LambdaQueryWrapper<AfVPrmCoop> wrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        wrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(afVPrmCoop.getCoopType())) {
            wrapper.in(AfVPrmCoop::getCoopType, afVPrmCoop.getCoopType().split(","));
        }
        if (StrUtil.isNotBlank(afVPrmCoop.getCoopName())) {
            wrapper.and(i -> i.like(AfVPrmCoop::getCoopCode, "%" + afVPrmCoop.getCoopName() + "%").or(j -> j.like(AfVPrmCoop::getCoopName, "%" + afVPrmCoop.getCoopName() + "%")).or(k -> k.like(AfVPrmCoop::getShortName, "%" + afVPrmCoop.getCoopName() + "%")));
        }

        if ("AE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeAe, afVPrmCoop.getBusinessScope());
        } else if ("AI".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeAi, afVPrmCoop.getBusinessScope());
        } else if ("SE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeSe, afVPrmCoop.getBusinessScope());
        } else if ("SI".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeSi, afVPrmCoop.getBusinessScope());
        } else if ("TE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeTE, afVPrmCoop.getBusinessScope());
        } else if ("LC".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeLC, afVPrmCoop.getBusinessScope());
        } else if ("VL".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeVL, afVPrmCoop.getBusinessScope());
        } else {
            throw new RuntimeException("请录入业务范畴");
        }
        return list(wrapper);
    }

    @Override
    public IPage getPage(Page page, AfVPrmCoop afVPrmCoop) {
        LambdaQueryWrapper<AfVPrmCoop> wrapper = Wrappers.<AfVPrmCoop>lambdaQuery();
        wrapper.eq(AfVPrmCoop::getOrgId, SecurityUtils.getUser().getOrgId());
        if (StrUtil.isNotBlank(afVPrmCoop.getCoopType())) {
            wrapper.in(AfVPrmCoop::getCoopType, afVPrmCoop.getCoopType().split(","));
        }
        if (StrUtil.isNotBlank(afVPrmCoop.getCoopName())) {
            wrapper.and(i -> i.like(AfVPrmCoop::getCoopCode, "%" + afVPrmCoop.getCoopName() + "%").or(j -> j.like(AfVPrmCoop::getCoopName, "%" + afVPrmCoop.getCoopName() + "%")).or(k -> k.like(AfVPrmCoop::getShortName, "%" + afVPrmCoop.getCoopName() + "%")));
        }

        if ("AE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeAe, afVPrmCoop.getBusinessScope());
        } else if ("AI".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeAi, afVPrmCoop.getBusinessScope());
        } else if ("SE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeSe, afVPrmCoop.getBusinessScope());
        } else if ("SI".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeSi, afVPrmCoop.getBusinessScope());
        } else if ("TE".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeTE, afVPrmCoop.getBusinessScope());
        } else if ("TI".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeTI, afVPrmCoop.getBusinessScope());
        } else if ("LC".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeLC, afVPrmCoop.getBusinessScope());
        } else if ("VL".equals(afVPrmCoop.getBusinessScope())) {
            wrapper.eq(AfVPrmCoop::getBusinessScopeVL, afVPrmCoop.getBusinessScope());
        } else {
            throw new RuntimeException("请录入业务范畴");
        }
        return page(page, wrapper);
    }
}

package com.efreight.afbase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.Nation;
import com.efreight.afbase.dao.NationMapper;
import com.efreight.afbase.service.NationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-17
 */
@Service
public class NationServiceImpl extends ServiceImpl<NationMapper, Nation> implements NationService {

    @Override
    public IPage queryPage(Page page, Nation nation) {
        QueryWrapper<Nation> queryWrapper = Wrappers.query();
        if (StrUtil.isNotBlank(nation.getNationCode())) {
            queryWrapper.eq("nation_code", nation.getNationCode());
        }
        if (StrUtil.isNotBlank(nation.getNationName())) {
            queryWrapper.like("nation_name", "%" + nation.getNationName() + "%");
        }

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Nation queryOne(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void importData(List<Nation> list) {
        list.stream().forEach(Nation -> {
            baseMapper.insert(Nation);
        });
    }
}

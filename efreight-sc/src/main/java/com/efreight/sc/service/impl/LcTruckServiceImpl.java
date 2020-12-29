package com.efreight.sc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.common.security.service.EUserDetails;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.sc.dao.LcTruckMapper;
import com.efreight.sc.entity.LcTruck;
import com.efreight.sc.entity.view.LcTruckExcel;
import com.efreight.sc.service.LcTruckService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * LC  车辆管理 服务实现类
 * </p>
 *
 * @author xiaobo
 * @since 2020-07-31
 */
@Service
public class LcTruckServiceImpl extends ServiceImpl<LcTruckMapper, LcTruck> implements LcTruckService {

    @Override
    public List<LcTruck> getList() {
        LambdaQueryWrapper<LcTruck> wrapper = Wrappers.<LcTruck>lambdaQuery();
        wrapper.eq(LcTruck::getOrgId, SecurityUtils.getUser().getOrgId()).eq(LcTruck::getIsValid, true);
        return list(wrapper);
    }

    @Override
    public int saveLcTruck(LcTruck lcTruck) {
        Integer orgId = SecurityUtils.getUser().getOrgId();
        LcTruck dbLcTruck = findByTruckNumber(orgId, lcTruck.getTruckNumber());
        if(null != dbLcTruck){
            throw new RuntimeException("车牌号已经存在");
        }

        lcTruck.setCreateTime(LocalDateTime.now());
        lcTruck.setCreatorId(SecurityUtils.getUser().getId());
        lcTruck.setCreatorName(SecurityUtils.getUser().buildOptName());
        lcTruck.setOrgId(orgId);

        return this.baseMapper.insert(lcTruck);
    }

    @Override
    public void insert(LcTruck lcTruck) {
        lcTruck.setCreateTime(LocalDateTime.now());
        lcTruck.setCreatorId(SecurityUtils.getUser().getId());
        lcTruck.setCreatorName(SecurityUtils.getUser().buildOptName());
        lcTruck.setEditTime(LocalDateTime.now());
        lcTruck.setEditorId(SecurityUtils.getUser().getId());
        lcTruck.setEditorName(SecurityUtils.getUser().buildOptName());
        lcTruck.setOrgId(SecurityUtils.getUser().getOrgId());
        save(lcTruck);
    }

    @Override
    public IPage<LcTruck> getPage(Page page, LcTruck lcTruck) {
        //获取查询条件
        LambdaQueryWrapper<LcTruck> wrapper = getWrapper(lcTruck);
        if (wrapper == null) {
            page.setRecords(new ArrayList());
            page.setTotal(0);
            return page;
        }
        //查询结果
        IPage<LcTruck> result = page(page, wrapper);
        processTruckOperate(result.getRecords());
        return result;
    }

    @Override
    public int delete(Integer truckId) {
        Assert.notNull(truckId, "非法参数");
        LcTruck lcTruck = new LcTruck();
        lcTruck.setTruckId(truckId);
        lcTruck.setIsValid(false);
        return this.baseMapper.updateById(lcTruck);
    }

    @Override
    public int update(LcTruck lcTruck) {
        EUserDetails userDetails = SecurityUtils.getUser();
        LcTruck dbLcTruck = findByTruckNumber(userDetails.getOrgId(), lcTruck.getTruckNumber());
        if(null != dbLcTruck && !dbLcTruck.getTruckId().equals(lcTruck.getTruckId())){
            throw new RuntimeException("车牌号已经存在");
        }

        lcTruck.setEditorName(userDetails.buildOptName());
        lcTruck.setEditorId(userDetails.getId());
        lcTruck.setEditTime(LocalDateTime.now());

        return this.baseMapper.updateByIdCanSetNull(lcTruck);
    }

    @Override
    public LcTruck findByTruckNumber(Integer orgId, String truckNumber) {
        org.springframework.util.Assert.hasText(truckNumber, "非法参数!");
        LambdaQueryWrapper<LcTruck> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(LcTruck::getOrgId, orgId);
        queryWrapper.eq(LcTruck::getTruckNumber, truckNumber);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public List<LcTruckExcel> queryListForExcel(LcTruck lcTruck) {
        LambdaQueryWrapper<LcTruck> wrapper = getWrapper(lcTruck);
        List<LcTruck> lcTruckList = this.baseMapper.selectList(wrapper);
        processTruckOperate(lcTruckList);

        List<LcTruckExcel> result = new ArrayList<>(lcTruckList.size());
        lcTruckList.stream().forEach((item)->{
            result.add(LcTruckExcel.build(item));
        });
        return result;
    }

    private LambdaQueryWrapper<LcTruck> getWrapper(LcTruck lcTruck) {
        LambdaQueryWrapper<LcTruck> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(LcTruck::getIsValid, true);
        if(StrUtil.isNotBlank(lcTruck.getTruckNumber())){
            wrapper.eq(LcTruck::getTruckNumber, lcTruck.getTruckNumber());
        }
        if(StrUtil.isNotBlank(lcTruck.getDriverName())){
            wrapper.eq(LcTruck::getDriverName, lcTruck.getDriverName());
        }
        Optional.ofNullable(lcTruck.getOrgId()).ifPresent((item)->wrapper.eq(LcTruck::getOrgId, lcTruck.getOrgId()));
        Optional.ofNullable(lcTruck.getLength()).ifPresent((item)-> wrapper.eq(LcTruck::getLength, lcTruck.getLength()));
        Optional.ofNullable(lcTruck.getTon()).ifPresent((item)->wrapper.eq(LcTruck::getTon, lcTruck.getTon()));

        wrapper.orderByDesc(LcTruck::getCreateTime);
        return wrapper;
    }

    private void processTruckOperate(List<LcTruck> lcTruckList){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lcTruckList.stream().forEach((item)->{
            item.setOperator(Optional.ofNullable(item.getEditorName()).orElse(item.getCreatorName()));
            item.setOperator(item.getOperator().split(" ")[0]);
            item.setOperateTime(Optional.ofNullable(item.getEditTime()).orElse(item.getCreateTime()).format(dateFormatter));
        });
    }
}

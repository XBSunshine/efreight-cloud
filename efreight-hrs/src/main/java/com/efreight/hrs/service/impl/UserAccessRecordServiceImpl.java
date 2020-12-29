package com.efreight.hrs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.hrs.dao.UserAccessRecordMapper;
import com.efreight.hrs.entity.UserAccessRecord;
import com.efreight.hrs.service.UserAccessRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author lc
 * @date 2020/5/12 14:59
 */
@Service
public class UserAccessRecordServiceImpl extends ServiceImpl<UserAccessRecordMapper, UserAccessRecord> implements UserAccessRecordService {

    @Override
    public void recordAccess(UserAccessRecord accessRecord) {
        Objects.requireNonNull(accessRecord.getUserId(), "数据参数为空");
        Assert.hasLength(accessRecord.getPath(), "数据参为空");
        //update
        int result = this.baseMapper.incrementRecord(accessRecord.getUserId(), accessRecord.getPath());
        //insert
        if(result == 0){
            Objects.requireNonNull(accessRecord.getOrgId(), "数据参数为空");
            Objects.requireNonNull(accessRecord.getPermissionId(), "数据参数为空");
            Assert.hasText(accessRecord.getPath(), "数据参数为空");

            accessRecord.setCreateTime(LocalDateTime.now());
            accessRecord.setEditTime(LocalDateTime.now());
            accessRecord.setRecordsNumber(1);
            this.baseMapper.insert(accessRecord);
        }
    }

    @Override
    public List<UserAccessRecord> topAccess(Integer userId, Integer number) {
        if(null == userId || null == number){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserAccessRecord>  lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(UserAccessRecord::getUserId, userId);
        lambdaQueryWrapper.orderByDesc(UserAccessRecord::getRecordsNumber, UserAccessRecord::getEditTime);
        lambdaQueryWrapper.last("limit 0, " + number);
        List<UserAccessRecord>  userAccessRecords = this.baseMapper.selectList(lambdaQueryWrapper);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd");
        userAccessRecords.stream().forEach((item)->
            item.setAccessTime(Optional.ofNullable(item.getEditTime()).orElse(item.getCreateTime()).format(dateTimeFormatter))
        );
        return userAccessRecords;
    }
}

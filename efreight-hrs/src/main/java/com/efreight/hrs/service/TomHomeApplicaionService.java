package com.efreight.hrs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.TomHomeApplication;

import java.util.List;

public interface TomHomeApplicaionService extends IService<TomHomeApplication> {
    List<TomHomeApplication> queryList(String type);

    void save(String applicationIds);

    List<Integer> getCheckList();
}

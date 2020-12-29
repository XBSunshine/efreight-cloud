package com.efreight.afbase.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.ScOrderMapper;
import com.efreight.afbase.entity.ScOrder;
import com.efreight.afbase.service.ScOrderService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScOrderServiceImpl extends ServiceImpl<ScOrderMapper, ScOrder> implements ScOrderService {
   
}

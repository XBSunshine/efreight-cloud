package com.efreight.afbase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.afbase.dao.VPrmCategoryMapper;
import com.efreight.afbase.entity.VPrmCategory;
import com.efreight.afbase.service.VPrmCategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class VPrmCategoryServiceImpl extends ServiceImpl<VPrmCategoryMapper, VPrmCategory> implements VPrmCategoryService {

}

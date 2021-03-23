package com.efreight.afbase.service.impl;

import com.efreight.afbase.entity.AfOrderStorageMns;
import com.efreight.afbase.dao.AfOrderStorageMnsMapper;
import com.efreight.afbase.service.AfOrderStorageMnsService;
import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qipm
 * @since 2020-10-26
 */
@Service
public class AfOrderStorageMnsServiceImpl extends ServiceImpl<AfOrderStorageMnsMapper, AfOrderStorageMns> implements AfOrderStorageMnsService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean doSave(AfOrderStorageMns bean) {
		bean.setCreatetime(LocalDateTime.now());
		baseMapper.insert(bean);
		if ("FOH".equals(bean.getFsutype())) {
			LocalDateTime Occurtime=LocalDateTime.now();
			if (!StringUtils.isEmpty(bean.getOccurtime())) {
				DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				Occurtime=LocalDateTime.parse(bean.getOccurtime(),df);
			}
			baseMapper.updateOrder(bean.getPcs(),bean.getGwt(),Occurtime,bean.getMawbcode());
		}
		
		return true;
	}

	@Override
	public List<AfOrderStorageMns> queryList(AfOrderStorageMns bean) {
		
		return baseMapper.queryList(bean);
	}

}

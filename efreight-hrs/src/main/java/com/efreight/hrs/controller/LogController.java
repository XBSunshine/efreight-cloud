package com.efreight.hrs.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.security.annotation.Inner;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/log")
public class LogController {
	private final LogService logService;

	@GetMapping("/getLogList")
	public MessageInfo getUserPage(Page page, Log bean) {
		return MessageInfo.ok(logService.getLogList(page, bean));
	}

	/**
	 * 保存日志
	 * @param bean
	 * @return
	 */
	@Inner
	@PostMapping("record")
	public MessageInfo record(@RequestBody Log bean){
		Assert.notNull(bean, "数据不能为空");
		Assert.notNull(bean.getCreatorId(), "操作人不能为空");
		Assert.notNull(bean.getOrgId(), "企业ID不能为空");
		bean.setCreateTime(LocalDateTime.now());
		boolean result = logService.save(bean);
		return MessageInfo.ok(result);
	}
}

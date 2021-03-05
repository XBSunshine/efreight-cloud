package com.efreight.afbase.controller;

import com.efreight.afbase.entity.view.SendProductEmail;
import com.efreight.afbase.service.AfProductService;
import com.efreight.afbase.utils.FilePathUtils;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * af 产品相关控制器
 * @author cwd
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/afProduct")
@Slf4j
public class AfProductController {
	private final AfProductService afProductService;
	
	/*
	 * 发送产品邮件
	 */
	@PostMapping(value="sendProductEmail")
	public MessageInfo sendProductEmail(@Valid @RequestBody SendProductEmail sendProductEmail) {
		try {
			boolean flag = afProductService.sendProductEmail(sendProductEmail);
			if(flag) {
				return MessageInfo.ok();
			}else {
				return MessageInfo.failed("发送失败");
			}
		} catch (Exception e) {
		   log.info(e.getMessage());
           return MessageInfo.failed(e.getMessage());
		}
	}

	@PostMapping("uploadFile")
	public MessageInfo uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName){
		if (file.isEmpty()) {
		    return MessageInfo.failed("文件不能为空");
		}

		Path path = Paths.get(FilePathUtils.filePath, "/PDFtemplate/temp/");
		Path dest = path.resolve(fileName);
		try {
			file.transferTo(dest);
			return MessageInfo.ok(dest.toFile().getAbsolutePath());
		} catch (IOException e) {
			log.error(e.toString(), e);
			return MessageInfo.failed(e.getMessage());
		}

	}

}

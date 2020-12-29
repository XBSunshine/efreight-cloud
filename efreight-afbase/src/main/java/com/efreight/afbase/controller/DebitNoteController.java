package com.efreight.afbase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.afbase.entity.DebitNote;
import com.efreight.afbase.entity.DebitNoteSendEntity;
import com.efreight.afbase.service.DebitNoteService;
import com.efreight.common.security.util.MessageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 清单 前端控制器
 * </p>
 *
 * @author xiaobo
 * @since 2019-11-07
 */
@RestController
@RequestMapping("/debitNote")
@AllArgsConstructor
@Slf4j
public class DebitNoteController {

	private final DebitNoteService debitNoteService;

	/**
	 * 列表分页查询
	 *
	 * @param page
	 * @param debitNote
	 * @return
	 */
	@GetMapping
	public MessageInfo page(Page page, DebitNote debitNote) {
		try {
			IPage result = debitNoteService.getPage(page, debitNote);
			return MessageInfo.ok(result);
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/page2")
	public MessageInfo page2(Page page, DebitNote debitNote) {

		IPage result = debitNoteService.getPage2(page, debitNote);
		return MessageInfo.ok(result);

	}

	@GetMapping("/select")
	public MessageInfo select(DebitNote debitNote) {
		try {

			return MessageInfo.ok(debitNoteService.select(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/select2")
	public MessageInfo select2(DebitNote debitNote) {
		try {

			return MessageInfo.ok(debitNoteService.select2(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/selectCheckDebit")
	public MessageInfo selectCheckDebit(DebitNote debitNote) {
		try {
			return MessageInfo.ok(debitNoteService.selectCheckDebit(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/selectOperation")
	public MessageInfo selectOperation(DebitNote debitNote) {
		try {

			return MessageInfo.ok(debitNoteService.selectOperation(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/selectOperation1")
	public MessageInfo selectOperation1(DebitNote debitNote) {
		try {

			return MessageInfo.ok(debitNoteService.selectOperation1(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@GetMapping("/selectOperation2")
	public MessageInfo selectOperation2(DebitNote debitNote) {
		try {

			return MessageInfo.ok(debitNoteService.selectOperation2(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单删除
	 *
	 * @param debitNote
	 * @return
	 */
	@PostMapping("/doDelete")
	public MessageInfo doDelete(DebitNote debitNote) {
		// @PostMapping("/doDelete/{debitNoteIds}")
		// public MessageInfo doDelete(@PathVariable String debitNoteIds){
		try {
			return MessageInfo.ok(debitNoteService.doDelete(debitNote));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单打印
	 *
	 * @param modelType
	 * @return
	 */
	@PostMapping("/print/{modelType}/{debitNoteId}")
	public MessageInfo print(@PathVariable String modelType, @PathVariable Integer debitNoteId) {
		try {
			return MessageInfo.ok(debitNoteService.print(modelType, debitNoteId, true, null));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单批量打印
	 *
	 * @param modelType
	 * @return
	 */
	@PostMapping("/printMany/{modelType}/{debitNoteIds}")
	public MessageInfo printMany(@PathVariable String modelType, @PathVariable String debitNoteIds) {
		try {
			return MessageInfo.ok(debitNoteService.printMany(modelType, debitNoteIds));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单批量打印New 存储过程
	 *
	 * @param modelType
	 * @param debitNoteIds
	 * @return
	 */
	@PostMapping("/printManynew/{modelType}/{debitNoteIds}/{businessScope}")
	public MessageInfo printManyNew(@PathVariable String modelType, @PathVariable String debitNoteIds,
			@PathVariable String businessScope) {
		try {
			return MessageInfo.ok(debitNoteService.printManyNew(modelType, debitNoteIds, businessScope));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单发送
	 *
	 * @param debitNoteSendEntity
	 * @return
	 */
	@PostMapping("/send")
	public MessageInfo send(@RequestBody DebitNoteSendEntity debitNoteSendEntity) {
		try {
			return MessageInfo.ok(debitNoteService.send(debitNoteSendEntity));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 清单修改时删除账单时触发 清除账单中的清单ID
	 *
	 * @param debitNoteNum
	 * @return
	 */
	@PostMapping("/deleteDebitNote/{debitNoteNum}")
	public MessageInfo deleteDebitNote(@PathVariable String debitNoteNum) {
		try {
			return MessageInfo.ok(debitNoteService.deleteDebitNote(debitNoteNum));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	@PostMapping("/updateDebitNote/{debitNoteId}/{statementId}")
	public MessageInfo updateDebitNote(@PathVariable Integer debitNoteId,@PathVariable Integer statementId) {
		try {
			return MessageInfo.ok(debitNoteService.updateDebitNote(debitNoteId,statementId));
		} catch (Exception e) {
			log.info(e.getMessage());
			return MessageInfo.failed(e.getMessage());
		}
	}

	/**
	 * 账单导出
	 *
	 * @param modelType
	 * @param debitNoteIds
	 * @param businessScope
	 * @return
	 */
	@PostMapping("/exportExcel/{modelType}/{debitNoteIds}/{businessScope}")
	public void exportExcel(@PathVariable String modelType, @PathVariable String debitNoteIds,
			@PathVariable String businessScope) {
		try {
			debitNoteService.exportExcel(modelType, debitNoteIds, businessScope);
		} catch (Exception e) {
			log.info(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 收入对账账单列表导出
	 *
	 * @param debitNote
	 * @return
	 */
	@PostMapping("/exportExcelList")
	public void exportExcelList(DebitNote debitNote) {
		try {
			debitNoteService.exportExcelList(debitNote);
		} catch (Exception e) {
			log.info(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

}

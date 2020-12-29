package com.efreight.prm.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efreight.common.core.utils.ExcelExportUtils;
import com.efreight.common.core.utils.ExportExcel;
import com.efreight.common.core.utils.FieldValUtils;
import com.efreight.common.security.util.MessageInfo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.prm.entity.*;
import com.efreight.prm.service.*;
import com.efreight.prm.util.MenuTreeCoop;
import com.efreight.prm.util.TreeCoopUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/coop")
@Slf4j
@AllArgsConstructor
public class CoopController {

    private final CoopService coopService;
    private final LogService logService;
    private final CoopAddressService addressService;
    private final CoopContactsService ContactsService;
    private final CoopScopeService ScopeService;
    private final CoopLockService LockService;
    private final CoopAgreementService agreementService;

    /**
     * 父节点列表查询
     *
     * @param currentPage
     * @param pageSize
     * @param coop
     * @return
     */
    @RequestMapping(value = "/queryCoopList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopList(Integer currentPage, Integer pageSize, @ModelAttribute("bean") Coop coop) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_code", coop.getCoop_code());
            paramMap.put("coop_mnemonic", coop.getCoop_mnemonic());
            paramMap.put("social_credit_code", coop.getSocial_credit_code());
            paramMap.put("coop_name", coop.getCoop_name());
            paramMap.put("coop_status", coop.getCoop_status());
            paramMap.put("black_valid", coop.getBlack_valid());
            paramMap.put("white_valid", coop.getWhite_valid());
            paramMap.put("credit_level", coop.getCredit_level());
            /*paramMap.put("org_id", SecurityUtils.getUser().getOrgId());

            paramMap.put("contacts_name", coop.getContacts_name());

            if (coop.getCoop_type() != null && !"".equals(coop.getCoop_type()))
                paramMap.put("coop_type", coop.getCoop_type().split(","));*/
            dataMap = coopService.queryCoopList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    @RequestMapping(value = "/queryCoopListByPage", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopListByPage(Integer currentPage, Integer pageSize, @ModelAttribute("bean") Coop coop) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_code", coop.getCoop_code());
            paramMap.put("coop_name", coop.getCoop_name());
            paramMap.put("lock_valid", coop.getLock_valid());
            paramMap.put("black_valid", coop.getBlack_valid());
            paramMap.put("white_valid", coop.getWhite_valid());
            paramMap.put("credit_level", coop.getCredit_level());
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            paramMap.put("transactor_user", coop.getTransactor_user());

            if (coop.getCoop_type() != null && !"".equals(coop.getCoop_type()))
                paramMap.put("coop_type", coop.getCoop_type().split(","));

            if (coop.getBusiness_scope() != null && !"".equals(coop.getBusiness_scope()))
                paramMap.put("business_scope", coop.getBusiness_scope().split(","));

            dataMap = coopService.queryCoopListByPage(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    @RequestMapping(value = "/queryCoopList1", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopList1(Coop coop) {
        String message = "success";
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
        paramMap.put("coop_code", coop.getCoop_code());
        paramMap.put("coop_mnemonic", coop.getCoop_mnemonic());
        paramMap.put("coop_name", coop.getCoop_name());
        paramMap.put("credit_level", coop.getCredit_level());
        paramMap.put("social_credit_code", coop.getSocial_credit_code());
        paramMap.put("contacts_name", coop.getContacts_name());
        paramMap.put("black_valid", coop.getBlack_valid());
        paramMap.put("white_valid", coop.getWhite_valid());
        paramMap.put("coop_status", coop.getCoop_status());
        if (coop.getCoop_type() != null && !"".equals(coop.getCoop_type()))
            paramMap.put("coop_type", coop.getCoop_type().split(","));
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(coopService.queryCoopList1(paramMap), message);
        messageInfo.setCode(200);
        return messageInfo;
    }

    /**
     * 子节点列表查询
     *
     * @param currentPage
     * @param pageSize
     * @param coop
     * @return
     */
    @RequestMapping(value = "/queryCoopChildList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopChildList(Integer currentPage, Integer pageSize, @ModelAttribute("bean") Coop coop) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_code", coop.getCoop_code());
            dataMap = coopService.queryCoopChildList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 树查询
     *
     * @param coop
     * @return
     */
    @RequestMapping(value = "/queryCoopTree", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopTree(@ModelAttribute("bean") Coop coop) {
        coop.setOrg_id(SecurityUtils.getUser().getOrgId());
        String message = "success";
        int code = 200;
        if (coop.getCoop_type() != null && !"".equals(coop.getCoop_type()))
//              paramMap.put("coop_type", coop.getCoop_type().split(","));
            coop.setCoop_types(coop.getCoop_type().split(","));
        Set<Coop> all = new HashSet<>();
        all.addAll(coopService.getTreeList(coop));
        System.out.println("++++++++++++++++"+all);
//		SecurityUtils.getRoles().forEach(roleId -> all.addAll(permissionService.getPermissionByRoleID(roleId)));
        List<MenuTreeCoop> menuTreeList = all.stream()
                .filter(menuVo -> true).map(MenuTreeCoop::new)
                .sorted(Comparator.comparingInt(MenuTreeCoop::getId)).collect(Collectors.toList());
        System.out.println(menuTreeList.size() + "   " + menuTreeList.toString());
        List<MenuTreeCoop> a = TreeCoopUtil.buildByLoop(menuTreeList, -1);
        System.out.println("==========="+a);
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(a, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 查询客商资料（新建页面使用）
     *
     * @return
     */
    @RequestMapping(value = "/queryListForChoose", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryListForChoose() {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            dataMap = coopService.queryListForChoose(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 查询客商资料（修改页面使用）
     *
     * @return
     */
    @RequestMapping(value = "/queryListForChooseForUpdate/{currOrgId}", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryListForChooseForUpdate(@PathVariable("currOrgId") Integer currOrgId) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            paramMap.put("currOrgId", currOrgId);
            dataMap = coopService.queryListForChooseForUpdate(paramMap);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 查询详情
     *
     * @param coop
     * @return
     */
    @RequestMapping(value = "/viewCoop", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo viewCoop(@ModelAttribute("bean") Coop coop) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        Coop coopre = new Coop();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coop.getCoop_id());
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
//			paramMap.put("dept_id", SecurityUtils.getUser().getDeptId());
            coopre = coopService.viewCoop(paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(coopre, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 新建
     *
     * @param coop
     * @return
     */

    @RequestMapping(value = "/saveCoop", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_add')")
    public com.efreight.prm.util.MessageInfo saveCoop(@ModelAttribute("bean") Coop coop) {
        System.out.println("coop_code:" + coop.getCoop_code());
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            coop.setCreator_id(SecurityUtils.getUser().getId());
            coop.setCreate_time(new Date());
            coop.setOrg_id(SecurityUtils.getUser().getOrgId());
            coop.setDept_id(SecurityUtils.getUser().getDeptId());

            //查询coop_code是否存在
            dataMap.put("coop_code", coop.getCoop_code());
            Coop recoop = coopService.queryCoopCodeByCoop(coop.getCoop_code());
            if (recoop != null)
                throw new Exception("该客商资料代码已经存在，请修改。");

            //查询客商资料中文全称是否存在
            Coop recoop1 = coopService.queryCoopCodeByCoopName(coop.getCoop_name());
            if (recoop1 != null)
                throw new Exception("该客商资料中文全称已经存在，请修改。");

            //查询客商资料中文简称是否存在
            /*Coop recoop2 = coopService.queryCoopCodeByShortName(coop.getShort_name());
            if (recoop2 != null)
                throw new Exception("该客商资料中文简称已经存在，请修改。");*/

            //查询客商资料英文全称是否存在
            Coop recoop3 = coopService.queryCoopCodeByCoopEName(coop.getCoop_ename());
            if (recoop3 != null)
                throw new Exception("该客商资料英文全称已经存在，请修改。");

            //查询客商资料英文简称是否存在
            /*Coop recoop4 = coopService.queryCoopCodeByShortEName(coop.getShort_ename());
            if (recoop4 != null)
                throw new Exception("该客商资料英文简称已经存在，请修改。");*/

            coop.setGroup_type("file");
            Integer coop_id = coopService.saveCoop(coop);

            dataMap.put("coop_id", coop.getCoop_id());

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        //添加日志
        try {
            LogBean logBean = new LogBean();
            logBean.setOp_level("高");
            logBean.setOp_type("新建");
            logBean.setOp_name("客商资料");
            logBean.setOp_info("新建客商资料：" + coop.getCoop_name() + " 客商资料代码：" + coop.getCoop_code());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("新建客商资料成功，添加日志失败");
        }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    @RequestMapping("/isHaveSocialCreditCode")
    public MessageInfo isHaveSocialCreditCode(Coop coop) {
        try {
            Integer serviceCount = coopService.isHaveSocialCreditCode(coop);
            if(serviceCount>0){
                return MessageInfo.ok();
            }else{
                return MessageInfo.failed();
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 新建分组
     *
     * @param coop
     * @return
     */

    @RequestMapping(value = "/saveSup", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_add')")
    public com.efreight.prm.util.MessageInfo saveSup(@ModelAttribute("bean") Coop coop) {
        System.out.println("coop_code:" + coop.getCoop_code());
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            coop.setCreator_id(SecurityUtils.getUser().getId());
            coop.setCreate_time(new Date());
            coop.setOrg_id(SecurityUtils.getUser().getOrgId());
            coop.setDept_id(SecurityUtils.getUser().getDeptId());

            int cl = coop.getCoop_code().length();
            //原来的客商资料代码
            String oriCoopCode = coop.getCoop_code().substring(0, cl - 6) + coop.getCoop_code().substring(cl - 3, cl);
            //分组代码
            String supCoopCode = coop.getCoop_code().substring(0, cl - 3);
            //先查询分组代码是否存在
            Coop recoop = coopService.queryCoopCodeByCoop(supCoopCode);
            if (recoop != null)
                throw new Exception("该客商资料代码已经存在，请修改。");

            //查询所有的客商资料代码
            List<Coop> list = coopService.queryCoopCodes(oriCoopCode);
            for (int i = 0; i < list.size(); i++) {
                Coop lcoop = list.get(i);
                //分组之后的客商资料代码
                String lcoopCode = lcoop.getCoop_code();
                String afCoopCode = supCoopCode + lcoopCode.substring(cl - 6, lcoopCode.length());
                if (afCoopCode.length() > 15) {
                    throw new Exception("超过层级限制，不能移动。");
                }
            }
            for (int i = 0; i < list.size(); i++) {
                Coop lcoop = list.get(i);
                //分组之后的客商资料代码
                String lcoopCode = lcoop.getCoop_code();
                String afCoopCode = supCoopCode + lcoopCode.substring(cl - 6, lcoopCode.length());
                //修改客商资料的代码
                coopService.modifyCoopCode(lcoopCode, afCoopCode);
            }
            coop.setCoop_code(supCoopCode);
            coop.setGroup_type("group");

            //保存分组
            Integer coop_id = coopService.saveCoop(coop);


            dataMap.put("coop_id", coop.getCoop_id());


        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        try {
            LogBean logBean = new LogBean();
            logBean.setOp_level("高");
            logBean.setOp_type("新建");
            logBean.setOp_name("客商资料分组");
            logBean.setOp_info("新建客商资料分组：" + coop.getCoop_name() + " 客商资料代码：" + coop.getCoop_code());
            logService.doSave(logBean);

        } catch (Exception e) {
            log.info("新建客商资料分组成功,添加日志失败");
        }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 修改
     *
     * @param coop
     * @return
     */

    @RequestMapping(value = "/modifyCoop")
    @PreAuthorize("@pms.hasPermission('sys_coop_edit')")
    public com.efreight.prm.util.MessageInfo modifyCoop(Coop coop) {
        System.out.println("coop_code:" + coop.getCoop_code());
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            coop.setEditor_id(SecurityUtils.getUser().getId());
            coop.setEdit_time(new Date());
            coop.setOrg_id(SecurityUtils.getUser().getOrgId());
            coop.setDept_id(SecurityUtils.getUser().getDeptId());

            //查询客商资料中文全称是否存在
            Coop recoop1 = coopService.queryCoopCodeByCoopName1(coop.getCoop_name(),coop.getCoop_id());
            if (recoop1 != null)
                throw new Exception("该客商资料中文全称已经存在，请修改。");

            //查询客商资料中文简称是否存在
            /*Coop recoop2 = coopService.queryCoopCodeByShortName1(coop.getShort_name(),coop.getCoop_id());
            if (recoop2 != null)
                throw new Exception("该客商资料中文简称已经存在，请修改。");*/

            //查询客商资料英文全称是否存在
            Coop recoop3 = coopService.queryCoopCodeByCoopEName1(coop.getCoop_ename(),coop.getCoop_id());
            if (recoop3 != null)
                throw new Exception("该客商资料英文全称已经存在，请修改。");

            //查询客商资料英文简称是否存在
            /*Coop recoop4 = coopService.queryCoopCodeByShortEName1(coop.getShort_ename(),coop.getCoop_id());
            if (recoop4 != null)
                throw new Exception("该客商资料英文简称已经存在，请修改。");*/

            Integer coop_id = coopService.modifyCoop(coop);
            dataMap.put("coop_id", coop.getCoop_id());
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }
        try {
            LogBean logBean = new LogBean();
            logBean.setOp_level("高");
            logBean.setOp_type("修改");
            logBean.setOp_name("客商资料");
            logBean.setOp_info("修改客商资料：" + coop.getCoop_name() + " 客商资料代码：" + coop.getCoop_code());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("修改客商资料成功，添加日志失败");
        }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 移动
     *
     * @return
     */

    @RequestMapping(value = "/move", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_move')")
    public com.efreight.prm.util.MessageInfo move(@RequestParam("subCoopCode") String subCoopCode, @RequestParam("coopCode") String coopCode) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            //先查询所有被移动的客商资料及其子集
            List<Coop> list = coopService.queryCoopCodes(coopCode);
            int ol = coopCode.length();
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    Coop coop = list.get(i);
                    String oricode = coop.getCoop_code();
                    int cl = oricode.length();
                    //分组之后的客商资料代码
                    String afCoopCode = subCoopCode + oricode.substring(ol - 3, cl);
                    if (afCoopCode.length() > 15) {
                        throw new Exception("超过层级限制，不能移动。");
                    }
                    Coop recoop = coopService.queryCoopCodeByCoop(afCoopCode);
                    if (recoop != null)
                        throw new Exception("该客商资料代码已经存在，不能移动。" + afCoopCode);

                }
                //移动
                for (int i = 0; i < list.size(); i++) {
                    Coop coop = list.get(i);
                    String oricode = coop.getCoop_code();
                    int cl = oricode.length();
                    //分组之后的客商资料代码
                    String afCoopCode = subCoopCode + oricode.substring(ol - 3, cl);
                    //修改客商资料的代码
                    coopService.modifyCoopCode(oricode, afCoopCode);

                    try {
                        LogBean logBean = new LogBean();
                        logBean.setOp_level("高");
                        logBean.setOp_type("修改");
                        logBean.setOp_name("客商资料");
                        logBean.setOp_info("修改客商资料代码：" + oricode + " 为：" + afCoopCode);
                        logService.doSave(logBean);
                    } catch (Exception e) {
                        log.info("修改客商资料代码：" + oricode + " 为：" + afCoopCode + "成功，添加日志失败");
                    }
                }
            }


        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    //地址-----------------------------------------------------

    /**
     * 查询地址
     *
     * @param currentPage
     * @param pageSize
     * @param coop_id
     * @return
     */
    @RequestMapping(value = "/queryCoopTabsList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopTabsList(Integer currentPage, Integer pageSize, String coop_id,String tabType) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coop_id);
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
//			paramMap.put("dept_id", SecurityUtils.getUser().getDeptId());
            dataMap = addressService.queryCoopTabsList(currentPage, pageSize, paramMap,tabType);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 删除地址
     * @param addr_id
     * @return
     */
    @PutMapping("/deleteAddress/{addr_id}")
    public com.efreight.prm.util.MessageInfo deleteAddress(@PathVariable("addr_id") Integer addr_id){
    	  String message = "success";
          int code = 200;
          Map<String, Object> dataMap = new HashMap();
          try {
        	  Map map = new HashMap();
	     	  map.put("addr_id", addr_id);
	     	  CoopAddressBean reCoopAddress = addressService.viewCoopAddress(map);
	     	  reCoopAddress.setAddr_status(-1);
	     	  reCoopAddress.setEditor_id(SecurityUtils.getUser().getId());
	     	  reCoopAddress.setEdit_time(new Date());
	     	  addressService.modifyCoopAddress(reCoopAddress);
          } catch (Exception e) {
              message = e.getMessage();
              code = 400;
          }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
          messageInfo.setCode(code);
          return messageInfo;
    }

    /**
     * 修改地址
     *
     * @param coopAddress
     * @return
     */
    @RequestMapping(value = "/modifyCoopAddress", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_address_edit')")
    public com.efreight.prm.util.MessageInfo modifyCoopAddress(CoopAddressBean coopAddress) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {

            coopAddress.setEditor_id(SecurityUtils.getUser().getId());
            coopAddress.setEdit_time(new Date());
            coopAddress.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopAddress.setDept_id(SecurityUtils.getUser().getDeptId());
//			coopAddress.setAddr_status(1);
            addressService.modifyCoopAddress(coopAddress);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 新建地址
     *
     * @param coopAddress
     * @return
     */
    @RequestMapping(value = "/saveCoopAddress", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_address_save')")
    public com.efreight.prm.util.MessageInfo saveCoopAddress(CoopAddressBean coopAddress) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            coopAddress.setCreator_id(SecurityUtils.getUser().getId());
            coopAddress.setCreate_time(new Date());
            coopAddress.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopAddress.setDept_id(SecurityUtils.getUser().getDeptId());
//			coopAddress.setAddr_status(1);
            addressService.saveCoopAddress(coopAddress);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }


    //联系人-----------------------------------------------------


    /**
     * 联系人查询
     *
     * @param currentPage
     * @param pageSize
     * @param coop_id
     * @return
     */
    @RequestMapping(value = "/queryCoopContactsList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopContactsList(Integer currentPage, Integer pageSize, String coop_id,String contacts_type) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coop_id);
            paramMap.put("contacts_type", contacts_type);
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
//			paramMap.put("dept_id", SecurityUtils.getUser().getDeptId());
            dataMap = ContactsService.queryCoopContactsList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 查询有效的联系人
     *
     * @param coopId
     * @return
     */
    @GetMapping("/queryContactsIsValid")
    public MessageInfo queryContactsIsValidByCoopId(String coopId) {
        try {
            List<CoopContactsBean> list = ContactsService.queryContactsIsValidByCoopId(coopId);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 删除联系人
     * @param contacts_id
     * @return
     */
    @PutMapping("/deleteContacts/{contacts_id}")
    public com.efreight.prm.util.MessageInfo deleteContacts(@PathVariable("contacts_id") Integer contacts_id){
    	  String message = "success";
          int code = 200;
          Map<String, Object> dataMap = new HashMap();
          try {
        	  Map map = new HashMap();
	     	  map.put("contacts_id", contacts_id);
	     	  CoopContactsBean coopContactsBean = ContactsService.viewCoopContacts(map);
	     	  coopContactsBean.setContacts_status(-1);
	     	  coopContactsBean.setEditor_id(SecurityUtils.getUser().getId());
	     	  coopContactsBean.setEdit_time(new Date());
	     	  ContactsService.modifyCoopContacts(coopContactsBean);
          } catch (Exception e) {
              message = e.getMessage();
              code = 400;
          }
        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
          messageInfo.setCode(code);
          return messageInfo;
    }
    /**
     * 联系人修改
     *
     * @param coopContacts
     * @return
     */
    @RequestMapping(value = "/modifyCoopContacts", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_contact_edit')")
    public com.efreight.prm.util.MessageInfo modifyCoopContacts(CoopContactsBean coopContacts) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {

            coopContacts.setEditor_id(SecurityUtils.getUser().getId());
            coopContacts.setEdit_time(new Date());
            coopContacts.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopContacts.setDept_id(SecurityUtils.getUser().getDeptId());
            //根据coop_id查询该客商资料已经存在哪些联系人邮箱
            List<CoopContactsBean> list = ContactsService.queryContactsIsValidByCoopId1(coopContacts.getCoop_id().toString(),coopContacts.getContacts_id());
            boolean existFlag = false;
            //判断邮箱是否已存在
            if(list!=null && list.size()>0){
                for(int j=0;j<list.size();j++){
                    if(list.get(j).getEmail() != null && list.get(j).getEmail().equals(coopContacts.getEmail())){//已存在
                        existFlag = true;
                    }
                }
            }
            if(!existFlag){
                ContactsService.modifyCoopContacts(coopContacts);
            }else{
                message = "邮箱已存在";
                code = 400;
            }

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 联系人新建
     *
     * @param coopContacts
     * @return
     */
    @RequestMapping(value = "/saveCoopContacts", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_contact_save')")
    public com.efreight.prm.util.MessageInfo saveCoopContacts(CoopContactsBean coopContacts) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        Integer contacts_id = null;
        try {
            coopContacts.setCreator_id(SecurityUtils.getUser().getId());
            coopContacts.setCreate_time(new Date());
            coopContacts.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopContacts.setDept_id(SecurityUtils.getUser().getDeptId());

            //判断邮箱是否有多个
            String email = coopContacts.getEmail();
            if(email!=null && !"".equals(email)){
                String email1 =email.replaceAll(" ","").replaceAll(",",";");
                String []emails = email1.split(";");
                if(emails.length>1){//多个
                    for(int i=0;i<emails.length;i++){
                        //根据coop_id查询该客商资料已经存在哪些联系人邮箱
                        List<CoopContactsBean> list = ContactsService.queryContactsIsValidByCoopId(coopContacts.getCoop_id().toString());
                        if(!"".equals(emails[i])){
                            boolean existFlag = false;
                            //判断邮箱是否已存在
                            if(list!=null && list.size()>0){
                                for(int j=0;j<list.size();j++){
                                    if(list.get(j).getEmail() != null && list.get(j).getEmail().toLowerCase().equals(emails[i].toLowerCase())){//已存在
                                        existFlag = true;
                                    }
                                }
                            }
                            if(!existFlag){
                                coopContacts.setEmail(emails[i]);
                                coopContacts.setContacts_name(emails[i].split("@")[0]);
                                ContactsService.saveCoopContacts(coopContacts);
                            }
                        }
                    }
                }else{
                    //根据coop_id查询该客商资料已经存在哪些联系人邮箱
                    List<CoopContactsBean> list = ContactsService.queryContactsIsValidByCoopId(coopContacts.getCoop_id().toString());
                    boolean existFlag = false;
                    //判断邮箱是否已存在
                    if(list!=null && list.size()>0){
                        for(int j=0;j<list.size();j++){
                            if(list.get(j).getEmail() != null && list.get(j).getEmail().toLowerCase().equals(emails[0].toLowerCase())){//已存在
                                existFlag = true;
                            }
                        }
                    }
                    if(!existFlag){
                        coopContacts.setEmail(emails[0]);
                        ContactsService.saveCoopContacts(coopContacts);
                    }else{
                        message = "邮箱已存在";
                        code = 400;
                    }
                }
            }
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        messageInfo.setData(contacts_id);
        return messageInfo;
    }

    /**
     * 联系人新建-账单模板新建
     *
     * @param coopContacts
     * @return
     */
    @RequestMapping(value = "/saveCoopContacts1", method = RequestMethod.POST)
    //@PreAuthorize("@pms.hasPermission('sys_coop_contact_save')")
    public com.efreight.prm.util.MessageInfo saveCoopContacts1(CoopContactsBean coopContacts) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        Integer contacts_id = null;
        try {
            coopContacts.setCreator_id(SecurityUtils.getUser().getId());
            coopContacts.setCreate_time(new Date());
            coopContacts.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopContacts.setDept_id(SecurityUtils.getUser().getDeptId());
            contacts_id = ContactsService.saveCoopContacts1(coopContacts);
        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        messageInfo.setData(contacts_id);
        return messageInfo;
    }

    //业务范畴------------------------------------------------------


    /**
     * 业务范畴查询
     *
     * @param currentPage
     * @param pageSize
     * @param coop_id
     * @return
     */
    @RequestMapping(value = "/queryCoopScopeList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopScopeList(Integer currentPage, Integer pageSize, String coop_id) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coop_id);
            //paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
//			paramMap.put("dept_id", SecurityUtils.getUser().getDeptId());
            dataMap = ScopeService.queryCoopScopeList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 业务范畴修改
     *
     * @param coopScope
     * @return
     */
    @RequestMapping(value = "/modifyCoopScope", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_scope_edit')")
    public com.efreight.prm.util.MessageInfo modifyCoopScope(CoopScopeBean coopScope) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {

            coopScope.setEditor_id(SecurityUtils.getUser().getId());
            coopScope.setEdit_time(new Date());
            coopScope.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopScope.setDept_id(SecurityUtils.getUser().getDeptId());
//			coopScope.setAddr_status(1);
            ScopeService.modifyCoopScope(coopScope);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 业务范畴新建
     *
     * @param coopScope
     * @return
     */
    @RequestMapping(value = "/saveCoopScope", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_scope_save')")
    public com.efreight.prm.util.MessageInfo saveCoopScope(CoopScopeBean coopScope) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            coopScope.setCreator_id(SecurityUtils.getUser().getId());
            coopScope.setCreate_time(new Date());
            coopScope.setOrg_id(SecurityUtils.getUser().getOrgId());
            coopScope.setDept_id(SecurityUtils.getUser().getDeptId());
//			coopScope.setAddr_status(1);
            ScopeService.saveCoopScope(coopScope);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    //异常记录-----------------------------------------------------


    /**
     * 异常记录查询
     *
     * @param currentPage
     * @param pageSize
     * @param coop_id
     * @return
     */
    @RequestMapping(value = "/queryCoopLockList", method = RequestMethod.POST)
    public com.efreight.prm.util.MessageInfo queryCoopLockList(Integer currentPage, Integer pageSize, String coop_id, String lock_type, String lock_reason) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coop_id);
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            paramMap.put("lock_type", lock_type);
            paramMap.put("lock_reason", lock_reason);
//				paramMap.put("dept_id", SecurityUtils.getUser().getDeptId());
            dataMap = LockService.queryCoopLockList(currentPage, pageSize, paramMap);

        } catch (Exception e) {
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    /**
     * 黑名单
     *
     * @param
     * @return
     */

    @PutMapping("/modifyBlackWhiteValid/0")
    @PreAuthorize("@pms.hasPermission('sys_coop_black')")
    public com.efreight.prm.util.MessageInfo modifyBlackValid(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {

            coopService.modifyBlackWhiteValid(param.get("coop_id").toString(), param.get("coop_code").toString(), "0", param.get("reason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 移除黑名单
     *
     * @param
     * @return
     */

    @PutMapping("/modifyOutBlackWhiteValid/0")
    //@PreAuthorize("@pms.hasPermission('sys_coop_black')")
    public com.efreight.prm.util.MessageInfo modifyOutBlackValid(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {

            coopService.modifyOutBlackWhiteValid(param.get("coop_id").toString(), param.get("coop_code").toString(), "0", param.get("outreason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 白名单
     *
     * @param
     * @return
     */

    @PutMapping("/modifyBlackWhiteValid/1")
    @PreAuthorize("@pms.hasPermission('sys_coop_white')")
    public com.efreight.prm.util.MessageInfo modifyWhiteValid(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {

            coopService.modifyBlackWhiteValid(param.get("coop_id").toString(), param.get("coop_code").toString(), "1", param.get("reason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 移除白名单
     *
     * @param
     * @return
     */

    @PutMapping("/modifyOutBlackWhiteValid/1")
    //@PreAuthorize("@pms.hasPermission('sys_coop_white')")
    public com.efreight.prm.util.MessageInfo modifyOutWhiteValid(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {

            coopService.modifyOutBlackWhiteValid(param.get("coop_id").toString(), param.get("coop_code").toString(), "1", param.get("outreason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 锁定
     *
     * @param
     * @return
     */

    @PutMapping("/lockOrUnlock/0")
    @PreAuthorize("@pms.hasPermission('sys_coop_lock')")
    public com.efreight.prm.util.MessageInfo lock(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {
            coopService.lockOrUnlock(param.get("coop_id").toString(), param.get("coop_code").toString(), "0", param.get("reason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 解锁
     *
     * @param
     * @return
     */

    @PutMapping("/lockOrUnlock/1")
    @PreAuthorize("@pms.hasPermission('sys_coop_unlock')")
    public com.efreight.prm.util.MessageInfo unlock(@RequestBody Map<String, Object> param) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {
            coopService.lockOrUnlock(param.get("coop_id").toString(), param.get("coop_code").toString(), "1", param.get("reason").toString());
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }

    /**
     * 导出Excel
     *
     * @param
     * @param response
     * @param bean
     * @throws IOException
     */

    @RequestMapping(value = "/exportExcel", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_export')")
    public void exportExcel(HttpServletResponse response, @ModelAttribute("bean") Coop bean) throws IOException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("coop_code", bean.getCoop_code());
        paramMap.put("coop_name", bean.getCoop_name());
        paramMap.put("lock_valid", bean.getLock_valid());
        paramMap.put("black_valid", bean.getBlack_valid());
        paramMap.put("white_valid", bean.getWhite_valid());
        paramMap.put("credit_level", bean.getCredit_level());
        paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
        paramMap.put("transactor_user", bean.getTransactor_user());

        if (bean.getCoop_type() != null && !"".equals(bean.getCoop_type()))
            paramMap.put("coop_type", bean.getCoop_type().split(","));

        if (bean.getBusiness_scope() != null && !"".equals(bean.getBusiness_scope()))
            paramMap.put("business_scope", bean.getBusiness_scope().split(","));

        List<CoopExcel> list = coopService.queryListForExcel(paramMap);


        if (!StringUtils.isEmpty(bean.getColumnStrs())) {
            List<LinkedHashMap> listExcel = new ArrayList<LinkedHashMap>();
            //转json为数组
            JSONArray jsonArr = JSONArray.parseArray(bean.getColumnStrs());
            String[] headers = new String[jsonArr.size()];
            String[] colunmStrs = new String[jsonArr.size()];

            //生成表头跟字段
            if (jsonArr != null && jsonArr.size() > 0) {
                for (int i = 0; i < jsonArr.size(); i++) {
                    JSONObject job = jsonArr.getJSONObject(i);
                    headers[i] = job.getString("label");
                    colunmStrs[i] = job.getString("prop");
                }
            }
            //遍历结果集 区相应字段封装 linkedHashMap后存储list发往工具类
            if (list != null && list.size() > 0) {
                for (CoopExcel coopExcel : list) {
                    LinkedHashMap map = new LinkedHashMap();
                    for (int j = 0; j < colunmStrs.length; j++) {
                        map.put(colunmStrs[j], FieldValUtils.getFieldValueByFieldName(colunmStrs[j], coopExcel));
                    }
                    listExcel.add(map);
                }
            }

            ExcelExportUtils u = new ExcelExportUtils();
            u.exportExcelLinkListMap(response, "导出EXCEL", headers, listExcel, "Export");

        }else{
            ExportExcel<CoopExcelForAll> ex = new ExportExcel<CoopExcelForAll>();
            List<CoopExcelForAll> coopList = new ArrayList<CoopExcelForAll>();
            if(list != null && list.size() > 0){
                for (CoopExcel coopExcel : list) {
                    CoopExcelForAll userExcelForAll = new CoopExcelForAll();
                    BeanUtils.copyProperties(coopExcel, userExcelForAll);
                    coopList.add(userExcelForAll);
                }
            }
            String[] headers = {"序号", "客商资料代码", "劳务类型", "中文全称", "英文全称", "社会信用代码", "开户行"
                    , "银行账号", "电话", "地址", "业务范畴" , "锁定时间", "锁定原因","是否黑名单", "黑名单原因", "是否白名单","白名单原因", "创建人", "创建时间", "修改人", "修改时间", "备注"};
            ex.exportExcel(response, "导出EXCEL", headers, coopList, "Export");
        }

        try {
            //导出日志数据
            LogBean logBean = new LogBean();
            logBean.setOp_level("低");
            logBean.setOp_type("导出");
            logBean.setOp_name("客商资料");
            logBean.setOp_info("导出客商资料列表：" + bean.getCoop_name());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("客商资料导出成功，添加日志失败");
        }
    }

    /**
     * 远程调用查询
     *
     * @param coopId
     * @return
     */
    @GetMapping("/{coopId}")
    public MessageInfo getOne(@PathVariable String coopId) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_id", coopId);
            Coop coop = coopService.viewCoop(paramMap);
            return MessageInfo.ok(coop);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 远程调用查询-通过类型查询列表
     *
     * @param coopType
     * @return
     */
    @GetMapping("/listByType/{coopType}")
    public MessageInfo listByType(@PathVariable("coopType") String coopType) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_type", coopType);
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            List<Coop> list = coopService.listByType(paramMap);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 远程调用查询-通过客户名称全模糊查询列表
     *
     * @param coopName
     * @return
     */
    @GetMapping("/listByCoopName/{coopName}")
    public MessageInfo listByCoopName(@PathVariable("coopName") String coopName) {
        try {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("coop_name", coopName);
            paramMap.put("org_id", SecurityUtils.getUser().getOrgId());
            List<Coop> list = coopService.listByCoopName(paramMap);
            return MessageInfo.ok(list);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 数据导入
     *
     * @param
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_import')")
    @ResponseBody
    public MessageInfo importCoopData(MultipartFile file) throws IOException {
        InputStream input = null;
        try {
            input = file.getInputStream();
            String fileAllName = file.getOriginalFilename();
            Workbook wb = null;
            if (fileAllName.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(input);
            } else if (fileAllName.endsWith(".xls")) {
                wb = new HSSFWorkbook(input);
            } else {
                throw new RuntimeException("导入文件格式有误！");
            }
            //获得第一个表单
            Sheet sheet = wb.getSheetAt(0);
            int minRowIx = sheet.getFirstRowNum() + 1;
            int maxRowIx = sheet.getLastRowNum();
            List<Coop> list = new ArrayList<Coop>();
            DecimalFormat df = new DecimalFormat("#");
            boolean isHaveErroe = false;//是否有错误标志，控制页面确认导入是否可用
            for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {

                Row row = sheet.getRow(rowIx);
                if (isEmptyRow(row)) {
                    continue;
                }
                String errorMessage = "";
                String coopCodeErrorFlag = "1";
                String coopTypeErrorFlag = "1";
                String coopNameErrorFlag = "1";
                String coopENameErrorFlag = "1";
                String socialCreditCodeErrorFlag = "1";
                String bankNameErrorFlag = "1";
                String bankNumberErrorFlag = "1";
                String phoneNumberErrorFlag = "1";
                String coopAddressErrorFlag = "1";
                String coopRemarkErrorFlag = "1";
                String fullAddressErrorFlag = "1";
                String contactsName1ErrorFlag = "1";
                String emailErrorFlag = "1";
                String phoneNumber1ErrorFlag = "1";
                String deptNameErrorFlag = "1";
                String jobPositionErrorFlag = "1";

                //客商资料代码
                String coop_code = "";
                if(row.getCell(0) !=null && !"".equals(row.getCell(0))){
                    if (row.getCell(0).getCellType() != CellType.BLANK) {
                        if (row.getCell(0).getCellType() == CellType.STRING) {
                            coop_code = row.getCell(0).getStringCellValue();
                        } else if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                            coop_code = df.format(row.getCell(0).getNumericCellValue());
                        }
                    }
                }
                if (StrUtil.isBlank(coop_code)) {
                    errorMessage += "客商资料代码必填;";
                    coopCodeErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_code)) {
                    if(coop_code.length() != 6){
                        errorMessage += "客商资料代码长度不是6位;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_code)) {
                    String regex = "^[a-z0-9A-Z]+$";
                    if(!coop_code.matches(regex)){
                        errorMessage += "客商资料代码不是只包含数字或字母;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_code)) {
                    //查询coop_code是否存在
                    Coop recoop = coopService.queryCoopCodeByCoop(coop_code.toUpperCase());
                    if (recoop != null){
                        errorMessage += "客商资料代码重复;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料类型
                String coop_type = "";
                if(row.getCell(1) !=null && !"".equals(row.getCell(1))){
                    if (row.getCell(1).getCellType() != CellType.BLANK) {
                        if (row.getCell(1).getCellType() == CellType.STRING) {
                            coop_type = row.getCell(1).getStringCellValue();
                        } else if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                            coop_type = df.format(row.getCell(1).getNumericCellValue());
                        }
                    }
                }
                if (StrUtil.isBlank(coop_type)) {
                    errorMessage += "客商资料类型必填;";
                    coopTypeErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_type)) {
                    if(!"外部客户".equals(coop_type) && !"互为代理".equals(coop_type) && !"干线承运人".equals(coop_type) && !"延伸服务供应商".equals(coop_type) && !"业务类结算对象".equals(coop_type) && !"非业务结算对象".equals(coop_type)){
                        errorMessage += "客商资料类型错误;";
                        coopTypeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料中文名称
                String coop_name = "";
                if(row.getCell(2) !=null && !"".equals(row.getCell(2))){
                    if (row.getCell(2).getCellType() != CellType.BLANK) {
                        if (row.getCell(2).getCellType() == CellType.STRING) {
                            coop_name = row.getCell(2).getStringCellValue();
                        } else if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                            coop_name = df.format(row.getCell(2).getNumericCellValue());
                        }
                    }
                }
                if (StrUtil.isBlank(coop_name)) {
                    errorMessage += "客商资料中文名称必填;";
                    coopNameErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_name)) {
                    Coop recoop1 = coopService.queryCoopCodeByCoopName(coop_name);
                    if (recoop1 != null){
                        errorMessage += "客商资料中文名称重复;";
                        coopNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_name)) {
                    if(coop_name.length() > 200){
                        errorMessage += "客商资料中文名称过长（200字符）;";
                        coopNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料英文名称
                String coop_ename = "";
                if(row.getCell(3) !=null && !"".equals(row.getCell(3))){
                    if (row.getCell(3).getCellType() != CellType.BLANK) {
                        if (row.getCell(3).getCellType() == CellType.STRING) {
                            coop_ename = row.getCell(3).getStringCellValue();
                        } else if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                            coop_ename = df.format(row.getCell(3).getNumericCellValue());
                        }
                    }
                }
                if (StrUtil.isBlank(coop_ename)) {
                    errorMessage += "客商资料英文名称必填;";
                    coopENameErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_ename)) {
                    Coop recoop2 = coopService.queryCoopCodeByCoopEName(coop_ename);
                    if (recoop2 != null){
                        errorMessage += "客商资料英文名称重复;";
                        coopENameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_ename)) {
                    if(coop_ename.length() > 200){
                        errorMessage += "客商资料英文名称过长（200字符）;";
                        coopENameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //社会信用代码
                String social_credit_code = "";
                if(row.getCell(4) !=null && !"".equals(row.getCell(4))){
                    if (row.getCell(4).getCellType() != CellType.BLANK) {
                        if (row.getCell(4).getCellType() == CellType.STRING) {
                            social_credit_code = row.getCell(4).getStringCellValue();
                        } else if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                            social_credit_code = df.format(row.getCell(4).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(social_credit_code)) {
                    if(social_credit_code.length() > 20){
                        errorMessage += "社会信用代码过长（20字符）;";
                        socialCreditCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //开户行
                String bank_name = "";
                if(row.getCell(5) !=null && !"".equals(row.getCell(5))){
                    if (row.getCell(5).getCellType() != CellType.BLANK) {
                        if (row.getCell(5).getCellType() == CellType.STRING) {
                            bank_name = row.getCell(5).getStringCellValue();
                        } else if (row.getCell(5).getCellType() == CellType.NUMERIC) {
                            bank_name = df.format(row.getCell(5).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(bank_name)) {
                    if(bank_name.length() > 40){
                        errorMessage += "开户行过长（40字符）;";
                        bankNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //银行账号
                String bank_number = "";
                if(row.getCell(6) !=null && !"".equals(row.getCell(6))){
                    if (row.getCell(6).getCellType() != CellType.BLANK) {
                        if (row.getCell(6).getCellType() == CellType.STRING) {
                            bank_number = row.getCell(6).getStringCellValue();
                        } else if (row.getCell(6).getCellType() == CellType.NUMERIC) {
                            bank_number = df.format(row.getCell(6).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(bank_number)) {
                    if(bank_number.length() > 40){
                        errorMessage += "银行账号过长（40字符）;";
                        bankNumberErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //手机号码
                String phone_number = "";
                if(row.getCell(7) !=null && !"".equals(row.getCell(7))){
                    if (row.getCell(7).getCellType() != CellType.BLANK) {
                        if (row.getCell(7).getCellType() == CellType.STRING) {
                            phone_number = row.getCell(7).getStringCellValue();
                        } else if (row.getCell(7).getCellType() == CellType.NUMERIC) {
                            phone_number = df.format(row.getCell(7).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(phone_number)) {
                    if(phone_number.length() > 20){
                        errorMessage += "手机号码过长（20字符）;";
                        phoneNumberErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //地址
                String coop_address = "";
                if(row.getCell(8) !=null && !"".equals(row.getCell(8))){
                    if (row.getCell(8).getCellType() != CellType.BLANK) {
                        if (row.getCell(8).getCellType() == CellType.STRING) {
                            coop_address = row.getCell(8).getStringCellValue();
                        } else if (row.getCell(8).getCellType() == CellType.NUMERIC) {
                            coop_address = df.format(row.getCell(8).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(coop_address)) {
                    if(coop_address.length() > 40){
                        errorMessage += "地址过长（40字符）;";
                        coopAddressErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //备注
                String coop_remark = "";
                if(row.getCell(9) !=null && !"".equals(row.getCell(9))){
                    if (row.getCell(9).getCellType() != CellType.BLANK) {
                        if (row.getCell(9).getCellType() == CellType.STRING) {
                            coop_remark = row.getCell(9).getStringCellValue();
                        } else if (row.getCell(9).getCellType() == CellType.NUMERIC) {
                            coop_remark = df.format(row.getCell(9).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(coop_remark)) {
                    if(coop_remark.length() > 200){
                        errorMessage += "备注过长（200字符）;";
                        coopRemarkErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //注册地址
                String full_address = "";
                if(row.getCell(10) !=null && !"".equals(row.getCell(10))){
                    if (row.getCell(10).getCellType() != CellType.BLANK) {
                        if (row.getCell(10).getCellType() == CellType.STRING) {
                            full_address = row.getCell(10).getStringCellValue();
                        } else if (row.getCell(10).getCellType() == CellType.NUMERIC) {
                            full_address = df.format(row.getCell(10).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(full_address)) {
                    if(full_address.length() > 80){
                        errorMessage += "注册地址过长（80字符）;";
                        fullAddressErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_姓名
                String contacts_name1 = "";
                if(row.getCell(11) !=null && !"".equals(row.getCell(11))){
                    if (row.getCell(11).getCellType() != CellType.BLANK) {
                        if (row.getCell(11).getCellType() == CellType.STRING) {
                            contacts_name1 = row.getCell(11).getStringCellValue();
                        } else if (row.getCell(11).getCellType() == CellType.NUMERIC) {
                            contacts_name1 = df.format(row.getCell(11).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(contacts_name1)) {
                    if(contacts_name1.length() > 40){
                        errorMessage += "联系人_姓名过长（40字符）;";
                        contactsName1ErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_邮箱
                String email = "";
                if(row.getCell(12) !=null && !"".equals(row.getCell(12))){
                    if (row.getCell(12).getCellType() != CellType.BLANK) {
                        if (row.getCell(12).getCellType() == CellType.STRING) {
                            email = row.getCell(12).getStringCellValue();
                        } else if (row.getCell(12).getCellType() == CellType.NUMERIC) {
                            email = df.format(row.getCell(12).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(email)) {
                    if(email.length() > 80){
                        errorMessage += "联系人_邮箱过长（80字符）;";
                        emailErrorFlag = "2";
                        isHaveErroe = true;
                    }else{
                        Pattern pattern=Pattern.compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
                        Matcher matcher = pattern.matcher(email);
                        if(!matcher.matches()){
                            errorMessage += "联系人_邮箱格式不正确;";
                            emailErrorFlag = "2";
                            isHaveErroe = true;
                        }
                        //String regx = "\\w+@\\w+(\\.[a-zA-Z]+)+";
                        //String regx = "/^[a-zA-Z#0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$/";
                        /*if(!email.matches(regx)){
                            errorMessage += "联系人_邮箱格式不正确;";
                            emailErrorFlag = "2";
                            isHaveErroe = true;
                        }*/
                    }
                }
                if((StrUtil.isBlank(contacts_name1) && !StrUtil.isBlank(email)) || (!StrUtil.isBlank(contacts_name1) && StrUtil.isBlank(email))){//联系人姓名、邮箱 必须都填写或者都不填写
                    errorMessage += "联系人姓名、邮箱 必须都填写或者都不填写;";
                    contactsName1ErrorFlag = "2";
                    emailErrorFlag = "2";
                    isHaveErroe = true;
                }
                //联系人_电话
                String phone_number1 = "";
                if(row.getCell(13) !=null && !"".equals(row.getCell(13))){
                    if (row.getCell(13).getCellType() != CellType.BLANK) {
                        if (row.getCell(13).getCellType() == CellType.STRING) {
                            phone_number1 = row.getCell(13).getStringCellValue();
                        } else if (row.getCell(13).getCellType() == CellType.NUMERIC) {
                            phone_number1 = df.format(row.getCell(13).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(phone_number1)) {
                    if(phone_number1.length() > 20){
                        errorMessage += "联系人_电话过长（20字符）;";
                        phoneNumber1ErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_部门
                String dept_name = "";
                if(row.getCell(14) !=null && !"".equals(row.getCell(14))){
                    if (row.getCell(14).getCellType() != CellType.BLANK) {
                        if (row.getCell(14).getCellType() == CellType.STRING) {
                            dept_name = row.getCell(14).getStringCellValue();
                        } else if (row.getCell(14).getCellType() == CellType.NUMERIC) {
                            dept_name = df.format(row.getCell(14).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(dept_name)) {
                    if(dept_name.length() > 40){
                        errorMessage += "联系人_部门过长（40字符）;";
                        deptNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_职务
                String job_position = "";
                if(row.getCell(15) !=null && !"".equals(row.getCell(15))){
                    if (row.getCell(15).getCellType() != CellType.BLANK) {
                        if (row.getCell(15).getCellType() == CellType.STRING) {
                            job_position = row.getCell(15).getStringCellValue();
                        } else if (row.getCell(15).getCellType() == CellType.NUMERIC) {
                            job_position = df.format(row.getCell(15).getNumericCellValue());
                        }
                    }
                }
                if (!StrUtil.isBlank(job_position)) {
                    if(job_position.length() > 40){
                        errorMessage += "联系人_职务过长（40字符）;";
                        jobPositionErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //excel数据内部检查客商资料代码，客商资料中文名称，客商资料英文名称是否有重复
                if(list != null && list.size()>0){
                    for(int j=0;j<list.size();j++){
                        if(coop_code.equals(list.get(j).getCoop_code())){
                            errorMessage += "导入文件中客商资料代码重复;";
                            coopCodeErrorFlag = "2";
                            isHaveErroe = true;
                        }
                        if(coop_name.equals(list.get(j).getCoop_name())){
                            errorMessage += "导入文件中客商资料中文名称重复;";
                            coopNameErrorFlag = "2";
                            isHaveErroe = true;
                        }
                        if(coop_ename.equals(list.get(j).getCoop_ename())){
                            errorMessage += "导入文件中客商资料英文名称重复;";
                            coopENameErrorFlag = "2";
                            isHaveErroe = true;
                        }
                    }
                }

                Coop bean = new Coop();
                bean.setCoop_code(coop_code);
                bean.setCoop_type(coop_type);
                bean.setCoop_name(coop_name);
                bean.setCoop_ename(coop_ename);
                bean.setSocial_credit_code(social_credit_code);
                bean.setBank_name(bank_name);
                bean.setBank_number(bank_number);
                bean.setPhone_number(phone_number);
                bean.setCoop_address(coop_address);
                bean.setCoop_remark(coop_remark);
                bean.setFull_address(full_address);
                bean.setContacts_name1(contacts_name1);
                bean.setEmail(email);
                bean.setPhone_number1(phone_number1);
                bean.setDept_name(dept_name);
                bean.setJob_position(job_position);

                bean.setCoopCodeErrorFlag(coopCodeErrorFlag);
                bean.setCoopTypeErrorFlag(coopTypeErrorFlag);
                bean.setCoopNameErrorFlag(coopNameErrorFlag);
                bean.setCoopENameErrorFlag(coopENameErrorFlag);
                bean.setSocialCreditCodeErrorFlag(socialCreditCodeErrorFlag);
                bean.setBankNameErrorFlag(bankNameErrorFlag);
                bean.setBankNumberErrorFlag(bankNumberErrorFlag);
                bean.setPhoneNumberErrorFlag(phoneNumberErrorFlag);
                bean.setCoopAddressErrorFlag(coopAddressErrorFlag);
                bean.setCoopRemarkErrorFlag(coopRemarkErrorFlag);
                bean.setFullAddressErrorFlag(fullAddressErrorFlag);
                bean.setContactsName1ErrorFlag(contactsName1ErrorFlag);
                bean.setEmailErrorFlag(emailErrorFlag);
                bean.setPhoneNumber1ErrorFlag(phoneNumber1ErrorFlag);
                bean.setDeptNameErrorFlag(deptNameErrorFlag);
                bean.setJobPositionErrorFlag(jobPositionErrorFlag);

                bean.setErrorMessage(errorMessage);
                list.add(bean);
            }
            //单个文件数据的最大行数为2000
            if(list != null && list.size()>2000){
                throw new RuntimeException("单个文件数据的最大行数不能超过2000");
            }

            if(isHaveErroe == true){
                return MessageInfo.ok(list,"haveError");
            }else{
                return MessageInfo.ok(list,"haveNoError");
            }
        } catch (Exception e) {
            return MessageInfo.failed(e.getMessage());
        } finally {
            //关闭
            input.close();
        }
    }

    /**
     * 确认导入
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/submitImport", method = RequestMethod.POST)
    @ResponseBody
    public MessageInfo submitImport(@RequestBody List<Coop> data){
        if(data != null && data.size()>0){
            boolean isHaveErroe = false;
            for (int i=0;i<data.size();i++) {

                Coop row = data.get(i);
                String errorMessage = "";
                String coopCodeErrorFlag = "1";
                String coopTypeErrorFlag = "1";
                String coopNameErrorFlag = "1";
                String coopENameErrorFlag = "1";
                String socialCreditCodeErrorFlag = "1";
                String bankNameErrorFlag = "1";
                String bankNumberErrorFlag = "1";
                String phoneNumberErrorFlag = "1";
                String coopAddressErrorFlag = "1";
                String coopRemarkErrorFlag = "1";
                String fullAddressErrorFlag = "1";
                String contactsName1ErrorFlag = "1";
                String emailErrorFlag = "1";
                String phoneNumber1ErrorFlag = "1";
                String deptNameErrorFlag = "1";
                String jobPositionErrorFlag = "1";

                //客商资料代码
                String coop_code = "";
                if(row !=null && !"".equals(row)){
                    coop_code = row.getCoop_code();
                }
                if (StrUtil.isBlank(coop_code)) {
                    errorMessage += "客商资料代码必填;";
                    coopCodeErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_code)) {
                    if(coop_code.length() != 6){
                        errorMessage += "客商资料代码长度不是6位;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_code)) {
                    String regex = "^[a-z0-9A-Z]+$";
                    if(!coop_code.matches(regex)){
                        errorMessage += "客商资料代码不是只包含数字或字母;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_code)) {
                    //查询coop_code是否存在
                    Coop recoop = coopService.queryCoopCodeByCoop(coop_code.toUpperCase());
                    if (recoop != null){
                        errorMessage += "客商资料代码重复;";
                        coopCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料类型
                String coop_type = "";
                if(row !=null && !"".equals(row)){
                    coop_type = row.getCoop_type();
                }
                if (StrUtil.isBlank(coop_type)) {
                    errorMessage += "客商资料类型必填;";
                    coopTypeErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_type)) {
                    if(!"外部客户".equals(coop_type) && !"互为代理".equals(coop_type) && !"干线承运人".equals(coop_type) && !"延伸服务供应商".equals(coop_type) && !"业务类结算对象".equals(coop_type) && !"非业务结算对象".equals(coop_type)){
                        errorMessage += "客商资料类型错误;";
                        coopTypeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料中文名称
                String coop_name = "";
                if(row !=null && !"".equals(row)){
                    coop_name = row.getCoop_name();
                }
                if (StrUtil.isBlank(coop_name)) {
                    errorMessage += "客商资料中文名称必填;";
                    coopNameErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_name)) {
                    Coop recoop1 = coopService.queryCoopCodeByCoopName(coop_name);
                    if (recoop1 != null){
                        errorMessage += "客商资料中文名称重复;";
                        coopNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_name)) {
                    if(coop_name.length() > 200){
                        errorMessage += "客商资料中文名称过长（200字符）;";
                        coopNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //客商资料英文名称
                String coop_ename = "";
                if(row !=null && !"".equals(row)){
                    coop_ename = row.getCoop_ename();
                }
                if (StrUtil.isBlank(coop_ename)) {
                    errorMessage += "客商资料英文名称必填;";
                    coopENameErrorFlag = "2";
                    isHaveErroe = true;
                }
                if (!StrUtil.isBlank(coop_ename)) {
                    Coop recoop2 = coopService.queryCoopCodeByCoopEName(coop_ename);
                    if (recoop2 != null){
                        errorMessage += "客商资料英文名称重复;";
                        coopENameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                if (!StrUtil.isBlank(coop_ename)) {
                    if(coop_ename.length() > 200){
                        errorMessage += "客商资料英文名称过长（200字符）;";
                        coopENameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //社会信用代码
                String social_credit_code = "";
                if(row !=null && !"".equals(row)){
                    social_credit_code = row.getSocial_credit_code();
                }
                if (!StrUtil.isBlank(social_credit_code)) {
                    if(social_credit_code.length() > 20){
                        errorMessage += "社会信用代码过长（20字符）;";
                        socialCreditCodeErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //开户行
                String bank_name = "";
                if(row !=null && !"".equals(row)){
                    bank_name = row.getBank_name();
                }
                if (!StrUtil.isBlank(bank_name)) {
                    if(bank_name.length() > 40){
                        errorMessage += "开户行过长（40字符）;";
                        bankNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //银行账号
                String bank_number = "";
                if(row !=null && !"".equals(row)){
                    bank_number = row.getBank_number();
                }
                if (!StrUtil.isBlank(bank_number)) {
                    if(bank_number.length() > 40){
                        errorMessage += "银行账号过长（40字符）;";
                        bankNumberErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //手机号码
                String phone_number = "";
                if(row !=null && !"".equals(row)){
                    phone_number = row.getPhone_number();
                }
                if (!StrUtil.isBlank(phone_number)) {
                    if(phone_number.length() > 20){
                        errorMessage += "手机号码过长（20字符）;";
                        phoneNumberErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //地址
                String coop_address = "";
                if(row !=null && !"".equals(row)){
                    coop_address = row.getCoop_address();
                }
                if (!StrUtil.isBlank(coop_address)) {
                    if(coop_address.length() > 40){
                        errorMessage += "地址过长（40字符）;";
                        coopAddressErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //备注
                String coop_remark = "";
                if(row !=null && !"".equals(row)){
                    coop_remark = row.getCoop_remark();
                }
                if (!StrUtil.isBlank(coop_remark)) {
                    if(coop_remark.length() > 200){
                        errorMessage += "备注过长（200字符）;";
                        coopRemarkErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //注册地址
                String full_address = "";
                if(row !=null && !"".equals(row)){
                    full_address = row.getFull_address();
                }
                if (!StrUtil.isBlank(full_address)) {
                    if(full_address.length() > 80){
                        errorMessage += "注册地址过长（80字符）;";
                        fullAddressErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_姓名
                String contacts_name1 = "";
                if(row !=null && !"".equals(row)){
                    contacts_name1 = row.getContacts_name1();
                }
                if (!StrUtil.isBlank(contacts_name1)) {
                    if(contacts_name1.length() > 40){
                        errorMessage += "联系人_姓名过长（40字符）;";
                        contactsName1ErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_邮箱
                String email = "";
                if(row !=null && !"".equals(row)){
                    email = row.getEmail();
                }
                if (!StrUtil.isBlank(email)) {
                    if(email.length() > 80){
                        errorMessage += "联系人_邮箱过长（80字符）;";
                        emailErrorFlag = "2";
                        isHaveErroe = true;
                    }else{
                        Pattern pattern=Pattern.compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
                        Matcher matcher = pattern.matcher(email);
                        if(!matcher.matches()){
                            errorMessage += "联系人_邮箱格式不正确;";
                            emailErrorFlag = "2";
                            isHaveErroe = true;
                        }
                    }
                }
                if((StrUtil.isBlank(contacts_name1) && !StrUtil.isBlank(email)) || (!StrUtil.isBlank(contacts_name1) && StrUtil.isBlank(email))){//联系人姓名、邮箱 必须都填写或者都不填写
                    errorMessage += "联系人姓名、邮箱 必须都填写或者都不填写;";
                    contactsName1ErrorFlag = "2";
                    emailErrorFlag = "2";
                    isHaveErroe = true;
                }
                //联系人_电话
                String phone_number1 = "";
                if(row !=null && !"".equals(row)){
                    phone_number1 = row.getPhone_number1();
                }
                if (!StrUtil.isBlank(phone_number1)) {
                    if(phone_number1.length() > 20){
                        errorMessage += "联系人_电话过长（20字符）;";
                        phoneNumber1ErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_部门
                String dept_name = "";
                if(row !=null && !"".equals(row)){
                    dept_name = row.getDept_name();
                }
                if (!StrUtil.isBlank(dept_name)) {
                    if(dept_name.length() > 40){
                        errorMessage += "联系人_部门过长（40字符）;";
                        deptNameErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }
                //联系人_职务
                String job_position = "";
                if(row !=null && !"".equals(row)){
                    job_position = row.getJob_position();
                }
                if (!StrUtil.isBlank(job_position)) {
                    if(job_position.length() > 40){
                        errorMessage += "联系人_职务过长（40字符）;";
                        jobPositionErrorFlag = "2";
                        isHaveErroe = true;
                    }
                }


                row.setCoopCodeErrorFlag(coopCodeErrorFlag);
                row.setCoopTypeErrorFlag(coopTypeErrorFlag);
                row.setCoopNameErrorFlag(coopNameErrorFlag);
                row.setCoopENameErrorFlag(coopENameErrorFlag);
                row.setSocialCreditCodeErrorFlag(socialCreditCodeErrorFlag);
                row.setBankNameErrorFlag(bankNameErrorFlag);
                row.setBankNumberErrorFlag(bankNumberErrorFlag);
                row.setPhoneNumberErrorFlag(phoneNumberErrorFlag);
                row.setCoopAddressErrorFlag(coopAddressErrorFlag);
                row.setCoopRemarkErrorFlag(coopRemarkErrorFlag);
                row.setFullAddressErrorFlag(fullAddressErrorFlag);
                row.setContactsName1ErrorFlag(contactsName1ErrorFlag);
                row.setEmailErrorFlag(emailErrorFlag);
                row.setPhoneNumber1ErrorFlag(phoneNumber1ErrorFlag);
                row.setDeptNameErrorFlag(deptNameErrorFlag);
                row.setJobPositionErrorFlag(jobPositionErrorFlag);

                row.setErrorMessage(errorMessage);
                //list.add(bean);
            }
            if(isHaveErroe == true){//有错误，返回
                return MessageInfo.ok(data,"haveError");
            }else{//没错误，进行插入
                coopService.importData(data);
                return MessageInfo.ok(data,"haveNoError");
            }
        }else{
            throw new RuntimeException("列表无数据");
            //return com.efreight.common.core.utils.MessageInfo.ok();
        }
    }

    @PostMapping("/downloadTemplate")
    public MessageInfo downloadTemplate(){
        try{
            String url = coopService.downloadTemplate();
            return MessageInfo.ok(url);
        }catch (Exception e){
            return MessageInfo.failed(e.getMessage());
        }
    }

    public boolean isEmptyRow(Row row) {
        if (row == null || row.toString().isEmpty()) {
            return true;
        } else {
            Iterator<Cell> it = row.iterator();
            boolean isEmpty = true;
            while (it.hasNext()) {
                Cell cell = it.next();
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    isEmpty = false;
                    break;
                }
            }
            return isEmpty;
        }
    }
    @RequestMapping(value = "/saveCoopAgreement", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_agreement_save')")
    public com.efreight.prm.util.MessageInfo saveCoopAgreement(CoopAgreementBean agreement) {
        String message = "success";
        int code = 200;
        Map<String, Object> dataMap = new HashMap();
        try {
            agreementService.saveCoopAgreement(agreement);
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            code = 400;
        }

        com.efreight.prm.util.MessageInfo messageInfo = new com.efreight.prm.util.MessageInfo(dataMap, message);
        messageInfo.setCode(code);
        return messageInfo;
    }

    @RequestMapping(value = "/stopAgreement", method = RequestMethod.POST)
    @PreAuthorize("@pms.hasPermission('sys_coop_agreement_stop')")
    public com.efreight.prm.util.MessageInfo stopAgreement(CoopAgreementBean agreement) {
        com.efreight.prm.util.MessageInfo messageInfo = null;
        try {
            agreementService.stopAgreement(agreement);
            messageInfo = new com.efreight.prm.util.MessageInfo(200, "success");
        } catch (Exception e) {
            messageInfo = new com.efreight.prm.util.MessageInfo(e);
        }
        return messageInfo;
    }
    /**
     * 远程调用查询-通过签约公司ID和客商资料代码查询客商资料是否已经存在
     *
     * @param orgId,coopCode
     * @return
     */
    @GetMapping("/getCoopCountByCode/{orgId}/{coopCode}")
    public MessageInfo getCoopCountByCode(@PathVariable("orgId") Integer orgId,@PathVariable("coopCode") String coopCode) {

        try {
            Coop coop = coopService.getCoopCountByCode(coopCode,orgId);
            return MessageInfo.ok(coop);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 远程调用查询-通过签约公司ID和客商资料名称查询客商资料是否已经存在
     *
     * @param orgId,coopCode
     * @return
     */
    @GetMapping("/getCoopCountByName/{orgId}/{coopName}")
    public MessageInfo getCoopCountByName(@PathVariable("orgId") Integer orgId,@PathVariable("coopName") String coopName) {
        try {
            Coop coop = coopService.getCoopCountByName(coopName,orgId);
            return MessageInfo.ok(coop);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 远程新建
     *
     * @param coop
     * @return
     */

    @RequestMapping(value = "/remoteSaveCoop", method = RequestMethod.POST)
    public MessageInfo remoteSaveCoop(@RequestBody Coop coop) {
        try {
            Integer coop_id = coopService.saveCoop1(coop);
            return MessageInfo.ok(coop.getCoop_id());
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }
    @GetMapping(value={"/checkSerialNumber/{serial_number}/{agreement_id}","/checkSerialNumber/{serial_number}"})
    public MessageInfo checkSerialNumber(
            @PathVariable(value = "agreement_id",required = false) Integer agreement_id
            ,@PathVariable("serial_number") String serial_number) {
        try {
            List<CoopAgreementBean> beans = agreementService.selectBySerialNumber(agreement_id,serial_number);
            return MessageInfo.ok(beans);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

    /**
     * 远程
     * @param orgId
     * @param bussinessScope
     * @return
     */
    @GetMapping("/selectPrmCoopsForAwb/{orgId}/{bussinessScope}")
    public MessageInfo selectPrmCoopsForAwb(@PathVariable("orgId") Integer orgId,@PathVariable("bussinessScope") String bussinessScope) {

        try {
            List<Coop> coops = coopService.selectPrmCoopsForAwb(orgId,bussinessScope);
            return MessageInfo.ok(coops);
        } catch (Exception e) {
            log.info(e.getMessage());
            return MessageInfo.failed(e.getMessage());
        }
    }

}


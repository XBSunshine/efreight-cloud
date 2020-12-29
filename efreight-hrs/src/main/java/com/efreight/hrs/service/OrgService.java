package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.hrs.entity.AFVPRMCategory;
import com.efreight.hrs.entity.Org;
import com.efreight.hrs.entity.OrgInterface;
import com.efreight.hrs.entity.SubOrgBean;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
public interface OrgService extends IService<Org> {
	Boolean saveOrg(Org org);

	IPage<Org> getOrgPage(Page page, Org org);

	Org getOrgByID(Integer orgId);
	
	Org getOneByCode(String orgCode);

	Boolean updateOrg(Org org);

	Boolean removeOrgById(Integer orgId);

	List<Org> listTrees();

	List<Org> listCurrentUserTrees();

    void resetPassward(Integer userId);

    List<Org> queryModelOrg();

	List<Org> getSignTemplate(Org org);

	List<OrgInterface> queryInterfaceList(Integer orgId);

	void saveInterface(OrgInterface orgInterface);

	void editInterface(OrgInterface orgInterface);

    /**
     * 获取配置信息
     * @param orgId 企业Id
     * @param apiType 配置类型
     * @return
     */
    OrgInterface getInterface(Integer orgId, String apiType);

    /**
     * 查询分类视图
     * @param categoryName 分类名
     * @return
     */
    List<AFVPRMCategory> listCategory(String categoryName);

	List<Org> listOrg();
	
	String saveEfOrgUser(String phoneArea,String phone,String email,String orgName,String passWordVerification,String userName,String orgFromRemark,String orgFromRemark2);
	
	Boolean orgConfigure(Org org);

	/**
	 * 启用意向用户
	 * @param orgId 企业ID
	 * @return
	 */
	int enabledIntendedUser(Integer orgId);

	/**
	 * 取消意向用户
	 * @param orgId 企业ID
	 * @return
	 */
	int disabledIntendedUser(Integer orgId);

	/**
	 * 设为非意向用户
	 * @param orgId 企业ID
	 * @return
	 */
	int unenabledIntendedUser(Integer orgId);

	Boolean getOrderFinanceLockView(Org org);
	Map getOrderFinanceLockViewNew(String businessScope);

	List<Org> listSubOrg(Integer orgId);

	Boolean deleteSubOrg(Integer orgId,Integer suborgCount);

	List<Org> selectSubOrg(Org org);

	void saveSubOrg(SubOrgBean subOrgBean);
	
	List<Org> getOrgChild(Org org);
	

}

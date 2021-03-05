package com.efreight.hrs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.common.security.vo.UserBaseVO;
import com.efreight.common.security.vo.UserInfo;
import com.efreight.common.security.vo.UserVo;
import com.efreight.hrs.entity.*;

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
public interface UserService extends IService<User> {
    void saveUser(UserVo userVo);

    IPage<User> getUserPage(Page page, UserVo userVo);

    UserVo getUserByID(Integer userId);

    UserInfo getUserInfo(User user);

    Boolean removeUserById(User user);

    Boolean updateUser(UserVo userVo);

    Boolean editPersonal(UserVo userVo);

    /**
     * 修改-离职
     *
     * @param userId
     * @param leaveDate
     * @param leaveReason
     */
    void leave(String userId, String leaveDate, String leaveReason);

    /**
     * 修改-复职
     *
     * @param userId
     */
    void resume(String userId);

    /**
     * 修改-黑名单
     *
     * @param userId
     * @param blackDate
     * @param blackReason
     */
    void black(String userId, String blackDate, String blackReason);

    List<UserExcel> queryListForExcel(UserVo bean);

    void resetPassward(Integer userId);

    void modifyPassward(Map<String, Object> map);

    List<UserVo> searchLoginNameAndOrgCode(UserVo bean);

    List<UserVo> searchLoginNameAndOrgCode1(UserVo bean);

    User getByOrgId(Integer orgId);

    User getUserInfoByUserEmail(String userEmail,Integer orgId);
    List<Map>searchUserByOrg(String orgCode);

    List<UserAddressExcel> queryListForAddressExcel(UserVo bean);
   
    List<UserVo> searchLoginNameAndOrgCode3(UserVo bean);

    User queryAdminByOrgId(Integer orgId);

    Boolean checkEmail(Map<String, Object> param);

    User getUserAboutKeepDecimalPlaces();

    void saveUserPageSet(UserPageSetVo userPageSetVo);

    List<UserPageSet> getUserPageSet(String pageName);

    List<Integer> getUserWorkgroupDetail(Integer userId);

    /**
     *  根据手机号+区号查询数据
     * @param phone 手机号
     * @param internationalCountryCode 区号
     * @return
     */
    UserBaseVO findByUserPhone(String phone, String internationalCountryCode);
}
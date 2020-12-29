package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.efreight.hrs.entity.Dept;
import com.efreight.hrs.entity.DeptExcel;
import com.efreight.hrs.entity.User;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.dao.DeptMapper;
import com.efreight.hrs.entity.UserMailCc;
import com.efreight.hrs.service.DeptService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghw
 * @since 2019-06-03
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDept(Dept dept) {
        dept.setCreateTime(LocalDateTime.now());
        dept.setCreatorId(SecurityUtils.getUser().getId());
        dept.setOrgId(SecurityUtils.getUser().getOrgId());
        String maxDeptCode = deptMapper.getMaxDeptCode(SecurityUtils.getUser().getOrgId(), dept.getDeptCode());

        if (maxDeptCode != null && maxDeptCode.length() > 0) {
            Long longDeptCode = Long.parseLong(maxDeptCode) + 1;
            dept.setDeptCode(String.valueOf(longDeptCode));
        } else {
            dept.setDeptCode(dept.getDeptCode() + "001");
        }
        baseMapper.insert(dept);
        if (dept.getIsFinalProfitunit()) {
            for (int i = 0; i < dept.getDeptCode().length(); i += 3) {
                String deptCode = dept.getDeptCode().substring(0, dept.getDeptCode().length() - 3 - i);
                baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(), deptCode, 1);
            }
        }
        return true;
    }

    @Override
    public IPage<Dept> getDeptPage(Page page, Dept Dept) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_Id", SecurityUtils.getUser().getOrgId());
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<Dept> getDeptList(Page page, Dept Dept) {
//		QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
//		if(Dept.getOrgId()!=null) {
//			queryWrapper.eq("org_id", Dept.getOrgId());
//		}
        IPage<Dept> pages = deptMapper.getDeptList(page, SecurityUtils.getUser().getOrgId(), Dept.getDeptStatus(), Dept.getDeptCode(), Dept.getDeptName());
        List<Dept> list = pages.getRecords();
        //List<DeptVO> resultList=new ArrayList<DeptVO>();
        for (int i = 0; i < list.size(); i++) {
            Dept bean = list.get(i);
            List<Dept> listc = deptMapper.getDeptListChildren(SecurityUtils.getUser().getOrgId(), Dept.getDeptStatus(), bean.getDeptCode(), Dept.getDeptName());
            if (listc.size() > 0) {
                bean.setHasChildren(true);
            } else {
                bean.setHasChildren(false);
            }
            //resultList.add(bean);
        }
        pages.setRecords(list);
        return pages;
    }

    @Override
    public List<Dept> getDeptListChildren(Dept Dept) {

        List<Dept> list = deptMapper.getDeptListChildren(SecurityUtils.getUser().getOrgId(), Dept.getDeptStatus(), Dept.getDeptCode(), Dept.getDeptName());
        //List<DeptVO> resultList=new ArrayList<DeptVO>();
        for (int i = 0; i < list.size(); i++) {
            Dept bean = list.get(i);
            List<Dept> listc = deptMapper.getDeptListChildren(SecurityUtils.getUser().getOrgId(), Dept.getDeptStatus(), bean.getDeptCode(), Dept.getDeptName());
            if (listc.size() > 0) {
                bean.setHasChildren(true);
            } else {
                bean.setHasChildren(false);
            }
            //resultList.add(bean);
        }
        return list;
    }

    @Override
    public List<User> selectUserByDeptId(String deptCode) {
        List<User> list = deptMapper.selectUserByDeptId(SecurityUtils.getUser().getOrgId(), deptCode);
        return list;
    }

    @Override
    public List<Dept> selectDeptByDeptCode(String deptCode) {
        List<Dept> list = deptMapper.selectDeptByDeptCode(SecurityUtils.getUser().getOrgId(), deptCode);
        return list;
    }

    @Override
    public List<Dept> getlList(String deptCode) {
        List<Dept> list = deptMapper.getlList(SecurityUtils.getUser().getOrgId(), deptCode);
        return list;
    }

    @Override
    public List<Dept> getlCList(String deptCode) {
        List<Dept> list = deptMapper.getlCList(SecurityUtils.getUser().getOrgId(), deptCode);
        return list;
    }

    @Override
    public List<Dept> getlCList2(String deptCode) {
        List<Dept> list = deptMapper.getlCList2(SecurityUtils.getUser().getOrgId(), deptCode);
        return list;
    }

    @Override
    public List<Dept> checkDeptName(String deptCode, String deptName) {
        List<Dept> list = deptMapper.checkDeptName(SecurityUtils.getUser().getOrgId(), deptCode, deptName);
        return list;
    }

    @Override
    public List<Dept> checkDeptShortName(String deptCode, String deptShortName) {
        List<Dept> list = deptMapper.checkDeptShortName(SecurityUtils.getUser().getOrgId(), deptCode, deptShortName);
        return list;
    }

    @Override
    public List<Map<String, Object>> selectUser(Dept dept) {
        Integer dept_id = null;
        if (dept != null) {
            dept_id = dept.getDeptId();
        }
        return deptMapper.selectUser(SecurityUtils.getUser().getOrgId(), dept_id);
    }

    @Override
    public List<String> selectOrderTrackCcUser(UserMailCc userMailCc) {
        userMailCc.setOrgId(SecurityUtils.getUser().getOrgId());
        userMailCc.setUserId(SecurityUtils.getUser().getId());
        return baseMapper.selectOrderTrackCcUser(userMailCc);
    }

    @Override
    public List<Integer> selectOrderTrackCcUserId(UserMailCc userMailCc) {
        userMailCc.setOrgId(SecurityUtils.getUser().getOrgId());
        userMailCc.setUserId(SecurityUtils.getUser().getId());
        return baseMapper.selectOrderTrackCcUserId(userMailCc);
    }

    @Override
    public List<Map<String, Object>> selectUserByCode(Dept dept) {
        String dept_code = null;
        if (dept != null) {
            dept_code = dept.getDeptCode().substring(0, dept.getDeptCode().length() - 3);
        }
        return deptMapper.selectUserByCode(SecurityUtils.getUser().getOrgId(), dept_code);
    }

    @Override
    public List<Dept> selectList(Dept dept) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        queryWrapper.eq("dept_status", 1);
        if (dept != null && dept.getIsFinalProfitunit()) {
            queryWrapper.eq("is_final_profitunit", 1);
        }
        return super.list(queryWrapper);
    }

    @Override
    public Dept getDeptByID(Integer deptId) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_Id", deptId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Dept getUserByDeptCode(String deptCode) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_code", deptCode);
        queryWrapper.eq("org_id", SecurityUtils.getUser().getOrgId());
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public String getAllMaxDeptCode(String deptCode) {
        return deptMapper.getAllMaxDeptCode(SecurityUtils.getUser().getOrgId(), deptCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDept(Dept dept) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_Id", dept.getDeptId());
        Dept oldDept = baseMapper.selectOne(queryWrapper);
        //
        dept.setEditTime(LocalDateTime.now());
        dept.setEditorId(SecurityUtils.getUser().getId());
        UpdateWrapper<Dept> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("dept_Id", dept.getDeptId());
        baseMapper.update(dept, updateWrapper);

        //修改下级显示名称
        baseMapper.updateChild(SecurityUtils.getUser().getOrgId(), dept.getDeptCode(), oldDept.getShortName() + "/", dept.getShortName() + "/");
        //是末端利润中心
        if (dept.getIsFinalProfitunit()) {
            for (int i = 0; i < dept.getDeptCode().length(); i += 3) {
                String deptCode = dept.getDeptCode().substring(0, dept.getDeptCode().length() - 3 - i);
                baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(), deptCode, 1);
            }
        } else {
//			for (int i = 0; i < dept.getDeptCode().length(); i+=3) {
//				//上级
//	    		String deptCode=dept.getDeptCode().substring(0, dept.getDeptCode().length()-3-i);
//	    		//本级
//	    		String deptCode2=dept.getDeptCode().substring(0, dept.getDeptCode().length()-3-i+3);
//	    		List<Dept> deptList=baseMapper.getListByDept(SecurityUtils.getUser().getOrgId(),deptCode,deptCode2);
//	    		if (deptList.size()>0) {
//					break;
//				}else{
//					baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(),deptCode,0);
//				}
//	    		
//			}
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeDeptById(Integer deptId) {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_Id", deptId);
        Dept dept = baseMapper.selectOne(queryWrapper);
        Map columnMap = new HashMap();
        columnMap.put("dept_Id", deptId);
        baseMapper.deleteByMap(columnMap);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(String deptCode, Boolean isFinalProfitunit) {
        baseMapper.deleteById2(SecurityUtils.getUser().getOrgId(), deptCode);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stopById(String deptCode, Boolean isFinalProfitunit) {
        baseMapper.stopById(SecurityUtils.getUser().getOrgId(), deptCode);
        //是末端利润中心
//		if (isFinalProfitunit) {
//			for (int i = 0; i < deptCode.length(); i+=3) {
//				//上级
//	    		String deptCodep=deptCode.substring(0, deptCode.length()-3-i);
//	    		//本级
//	    		String deptCode2=deptCode.substring(0, deptCode.length()-3-i+3);
//	    		List<Dept> deptList=baseMapper.getListByDept(SecurityUtils.getUser().getOrgId(),deptCodep,deptCode2);
//	    		if (deptList.size()>0) {
//					break;
//				}else{
//					baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(),deptCodep,0);
//				}
//	    		
//			}
//		}
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startById(String deptCode, Boolean isFinalProfitunit) {
        baseMapper.startById(SecurityUtils.getUser().getOrgId(), deptCode);
        //是末端利润中心
        if (isFinalProfitunit) {
            for (int i = 0; i < deptCode.length(); i += 3) {
                String deptCodep = deptCode.substring(0, deptCode.length() - 3 - i);
                baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(), deptCodep, 1);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sortById(String deptCode, String deptCode3) {
        String[] deptCodes = deptCode.split(",");
        String[] deptCode3s = deptCode3.split(",");
        //删除
        baseMapper.deleteChild(SecurityUtils.getUser().getOrgId(), deptCodes[0].substring(0, deptCodes[0].length() - 3));
        //修改
        for (int i = 0; i < deptCode3s.length; i++) {
            String deptCodeStr = deptCodes[i].substring(0, deptCodes[i].length() - 3) + deptCode3s[i];
            deptMapper.moveDept(SecurityUtils.getUser().getOrgId(), deptCodeStr, deptCodes[i], deptCodeStr.length() + 1);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean mergeById(String deptCode, String deptCodeSelect, Boolean isFinalProfitunit) {
        Dept dept = getUserByDeptCode(deptCode);
        Dept deptSelect = getUserByDeptCode(deptCodeSelect);
        baseMapper.updateUserOfDept(SecurityUtils.getUser().getOrgId(), dept.getDeptId(), deptSelect.getDeptId());
//		baseMapper.updateDeptStatus(SecurityUtils.getUser().getOrgId(),deptSelect.getDeptId());
        baseMapper.deleteDeptStatus(SecurityUtils.getUser().getOrgId(), deptSelect.getDeptId());


        //是末端利润中心
//		if (isFinalProfitunit) {
//			for (int i = 0; i < deptCode.length(); i+=3) {
//				String deptCodep=deptCode.substring(0, deptCode.length()-3-i);
//				baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(),deptCodep,1);
//			}			
//		}
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveById(String deptCode, String deptCodeSelect, Boolean isFinalProfitunit, String oldFullName, String newFullName) {
        //修改下级显示名称
        baseMapper.updateChild(SecurityUtils.getUser().getOrgId(), deptCodeSelect, oldFullName.substring(0, oldFullName.lastIndexOf("/")) + "/", newFullName + "/");
        String maxDeptCode = deptMapper.getMaxDeptCode(SecurityUtils.getUser().getOrgId(), deptCode);
        List<Dept> list = deptMapper.getlCList(SecurityUtils.getUser().getOrgId(), deptCodeSelect);
        if (maxDeptCode != null && maxDeptCode.length() > 0) {
            Long longDeptCode = Long.parseLong(maxDeptCode) + 1;
            deptMapper.moveDept(SecurityUtils.getUser().getOrgId(), String.valueOf(longDeptCode), deptCodeSelect, deptCodeSelect.length() + 1);
        } else {
            //deptMapper.moveDept(SecurityUtils.getUser().getOrgId(),deptCode,deptCodeSelect);
            deptMapper.moveDept(SecurityUtils.getUser().getOrgId(), deptCode, deptCodeSelect, deptCodeSelect.length() - 2);
        }

        //是末端利润中心
        if (isFinalProfitunit || list.size() > 0) {
            for (int i = 0; i < deptCode.length(); i += 3) {
                String deptCodep = deptCode.substring(0, deptCode.length() - i);
                baseMapper.updateIsProfitunitByDeptCode(SecurityUtils.getUser().getOrgId(), deptCodep, 1);
            }
        }
        return true;
    }

    @Override
    public List<Dept> listTrees() {
        // TODO Auto-generated method stub
        return this.list();
    }

    @Override
    public List<Dept> listCurrentUserTrees() {
        Integer deptId = SecurityUtils.getUser().getDeptId();

        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_Id", deptId);
        Dept dept = baseMapper.selectOne(queryWrapper);
        QueryWrapper<Dept> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.like("dept_Code", dept.getDeptCode() + "%");
        return baseMapper.selectList(queryWrapper1);
    }

    @Override
    public List<Dept> getDeptbyOrgid(Dept dept) {
//		QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
//		queryWrapper.eq("org_Id", SecurityUtils.getUser().getOrgId());
        List<Dept> depts = baseMapper.getDeptbyOrgid(SecurityUtils.getUser().getOrgId(), dept.getDeptName(), dept.getDeptStatus());
     //2020-2-18新需求 新增跟编辑页面保持一致 显示名称为上下级
//        depts.stream().forEach(dept1 -> {
//            if (StrUtil.isNotBlank(dept1.getFullName()) && dept1.getFullName().indexOf("/") != -1) {
//                dept1.setFullName(dept1.getFullName().substring(dept1.getFullName().indexOf("/") + 1, dept1.getFullName().length()));
//            }
//        });
        return depts;
    }

    @Override
    public List<DeptExcel> queryListForExcel(Dept bean) {
        bean.setOrgId(SecurityUtils.getUser().getOrgId());
        return baseMapper.queryListForExcel(bean);
    }

    @Override
    public List<User> queryUserList(String deptCode) {
        return baseMapper.queryUserList(SecurityUtils.getUser().getOrgId(), deptCode);
    }
}

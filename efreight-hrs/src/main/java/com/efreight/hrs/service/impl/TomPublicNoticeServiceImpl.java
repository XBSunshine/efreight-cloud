package com.efreight.hrs.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.efreight.common.core.jms.MailSendService;
import com.efreight.common.security.vo.UserVo;
import com.efreight.common.security.util.SecurityUtils;
import com.efreight.hrs.entity.Log;
import com.efreight.hrs.entity.TomPublicNotice;
import com.efreight.hrs.dao.TomPublicNoticeMapper;
import com.efreight.hrs.entity.User;
import com.efreight.hrs.service.LogService;
import com.efreight.hrs.service.TomPublicNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.efreight.hrs.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author wangxx
 * @since 2019-07-15
 */
@Slf4j
@Service
@AllArgsConstructor
public class TomPublicNoticeServiceImpl extends ServiceImpl<TomPublicNoticeMapper, TomPublicNotice> implements TomPublicNoticeService {

    private final MailSendService mailSendService;
    private final LogService logService;
    private final UserService userService;


    /**
     * 分页查询列表
     *
     * @param page
     * @param tomPublicNotice
     * @return
     */
    @Override
    public IPage<TomPublicNotice> getTomPublicNoticeListPage(Page<TomPublicNotice> page, TomPublicNotice tomPublicNotice) {
        QueryWrapper<TomPublicNotice> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(tomPublicNotice.getNoticeTitle())) {
            queryWrapper.like("notice_title", "%" + tomPublicNotice.getNoticeTitle() + "%");
        }
        if (StrUtil.isNotBlank(tomPublicNotice.getNoticeType())) {
            queryWrapper.eq("notice_type", tomPublicNotice.getNoticeType());
        }
        queryWrapper.orderByDesc("notice_date");
        return baseMapper.selectPage(page, queryWrapper);

    }

    /**
     * 查看详情
     *
     * @param noticeId
     * @return
     */
    @Override
    public TomPublicNotice getTomPublicNotice(Integer noticeId) {
        return baseMapper.selectById(noticeId);
    }

    /**
     * 新建
     *
     * @param tomPublicNotice
     */
    @Override
    public void saveTomPublicNotice(TomPublicNotice tomPublicNotice) {
        tomPublicNotice.setCreateTime(LocalDateTime.now());
        save(tomPublicNotice);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("新建");
            logBean.setOpName("系统公告");
            logBean.setOpInfo("系统公告新建:" + tomPublicNotice.getNoticeText());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("系统公告新建成功，日志添加失败");
        }
        if("saveAndSend".equals(tomPublicNotice.getSendFlag())){
            try {
                sendMail(tomPublicNotice);
            } catch (Exception e) {
                throw new RuntimeException("保存成功，邮件发送异常");
            }
        }
    }

    /**
     * 首页展示
     *
     * @return
     */
    @Override
    public List<TomPublicNotice> getListForHomePage() {
        User user = userService.getById(SecurityUtils.getUser().getId());
        QueryWrapper<TomPublicNotice> queryWrapper = Wrappers.query();
        if (user.getIsadmin()) {
            queryWrapper.in("push_type", 1, 2).or(i -> i.eq("push_type", 3).eq("push_org", SecurityUtils.getUser().getOrgId()));
            queryWrapper.orderByDesc("notice_date").orderByDesc("create_time").last("limit 10");
        } else {
            queryWrapper.in("push_type", 1).or(i -> i.eq("push_type", 3).eq("push_org", SecurityUtils.getUser().getOrgId()));
            queryWrapper.orderByDesc("notice_date").orderByDesc("create_time").last("limit 10");
        }

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public void updateNoticeById(TomPublicNotice tomPublicNotice) {
        baseMapper.updateById(tomPublicNotice);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("修改");
            logBean.setOpName("系统公告");
            logBean.setOpInfo("系统公告修改:" + tomPublicNotice.getNoticeId());
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("系统公告修改成功，日志添加失败");
        }
    }

    @Override
    public void removeNoticeById(String noticeId) {
        baseMapper.deleteById(noticeId);
        try {
            Log logBean = new Log();
            logBean.setOpLevel("高");
            logBean.setOpType("删除");
            logBean.setOpName("系统公告");
            logBean.setOpInfo("系统公告删除:" + noticeId);
            logService.doSave(logBean);
        } catch (Exception e) {
            log.info("系统公告删除成功，日志添加失败");
        }
    }

    /**
     * 发送邮件
     */
    private void sendMail(TomPublicNotice tomPublicNotice) {
        //密码发送邮件给客户
        String file = tomPublicNotice.getNoticeFile();
        String title = tomPublicNotice.getNoticeTitle();
        String text = tomPublicNotice.getNoticeText();
        String type = tomPublicNotice.getPushType();
        Integer org = tomPublicNotice.getPushOrg();

        List<User> list = null;
        if ("1".equals(type)) {
            //查所有用户
            list = baseMapper.findAllUser();
        } else if ("2".equals(type)) {
            //查所有管理员
            list = baseMapper.findAllAdmin();
        } else if ("3".equals(type)) {
            //查某个签约公司的所有用户
            list = baseMapper.findAllUserByOrgId(org);
        }
        list.stream().forEach(user -> {
            if (StrUtil.isNotBlank(user.getUserEmail())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UserVo userVo = new UserVo();
                            BeanUtils.copyProperties(userService.getById(1),userVo);
                            mailSendService.sendHtmlMailNewForHrs(false,new String[]{user.getUserEmail()},null, null, title, text,null,userVo);
                        } catch (Exception e) {
                            log.info(user.getUserEmail() + "发送失败：" + e.getMessage());
                        }
                    }
                }).start();
            }
        });

    }
}

package com.novaboot.module.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.common.exception.BusinessException;
import com.novaboot.framework.captcha.CaptchaService;
import com.novaboot.module.system.dto.LoginBody;
import com.novaboot.module.system.dto.LoginVO;
import com.novaboot.module.system.dto.UserProfileVO;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認證服務
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CaptchaService captchaService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginBody body) {
        captchaService.verify(body.getCaptchaId(), body.getCaptchaCode());
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, body.getUsername()));
        if (user == null || !passwordEncoder.matches(body.getPassword(), user.getPassword())) {
            throw new BusinessException("用戶名或密碼錯誤");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("賬號已停用，請聯繫管理員");
        }
        StpUtil.login(user.getId());
        return new LoginVO(StpUtil.getTokenValue());
    }

    public void logout() {
        StpUtil.logout();
    }

    public UserProfileVO profile() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用戶不存在");
        }
        return new UserProfileVO(
                new UserProfileVO.UserInfo(user.getId(), user.getUsername(), user.getNickname()),
                StpUtil.getRoleList(userId),
                StpUtil.getPermissionList(userId));
    }
}

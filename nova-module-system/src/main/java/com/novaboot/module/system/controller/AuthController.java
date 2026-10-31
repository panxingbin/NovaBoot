package com.novaboot.module.system.controller;

import com.novaboot.common.core.domain.Result;
import com.novaboot.framework.captcha.CaptchaService;
import com.novaboot.framework.captcha.CaptchaVO;
import com.novaboot.module.system.dto.LoginBody;
import com.novaboot.module.system.dto.LoginVO;
import com.novaboot.module.system.dto.UserProfileVO;
import com.novaboot.module.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認證接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.ok(captchaService.generate());
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginBody body) {
        return Result.ok(authService.login(body));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.ok(authService.profile());
    }
}

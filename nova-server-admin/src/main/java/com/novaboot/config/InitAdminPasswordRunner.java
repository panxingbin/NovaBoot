package com.novaboot.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 內置管理員密碼兜底初始化：
 * 僅當密碼不是 BCrypt 格式、或仍是 Flyway 種子哈希（未被修改過）時，同步為配置的初始密碼。
 * 管理員一旦自行修改過密碼，啟動器不再覆蓋。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitAdminPasswordRunner implements ApplicationRunner {

    /** Flyway 種子數據中的默認哈希（對應 admin123），僅用於判斷「密碼從未被修改」 */
    private static final String SEED_HASH = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${nova.init.admin-password:admin123}")
    private String initPassword;

    @Override
    public void run(ApplicationArguments args) {
        SysUser admin = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "admin"));
        if (admin == null) {
            return;
        }
        String password = admin.getPassword();
        boolean notBcrypt = password == null || !password.startsWith("$2");
        boolean stillSeedHash = SEED_HASH.equals(password);
        if (notBcrypt || stillSeedHash) {
            admin.setPassword(passwordEncoder.encode(initPassword));
            sysUserMapper.updateById(admin);
            log.info("admin 初始密碼已同步為配置值 nova.init.admin-password");
        }
    }
}

package com.novaboot.module.system.log;

import cn.dev33.satoken.stp.StpUtil;
import com.novaboot.framework.util.ServletUtils;
import com.novaboot.module.system.annotation.OperLog;
import com.novaboot.module.system.entity.SysOperLog;
import com.novaboot.module.system.entity.SysUser;
import com.novaboot.module.system.mapper.SysUserMapper;
import com.novaboot.module.system.service.OperLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 操作日誌切面：記錄標註了 @OperLog 的 Controller 方法調用（異步落庫）
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogService operLogService;
    private final SysUserMapper sysUserMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog record = new SysOperLog();
        record.setTitle(operLog.title());
        record.setBusinessType(operLog.businessType().getValue());
        Signature signature = joinPoint.getSignature();
        record.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            record.setRequestMethod(request.getMethod());
            record.setUrl(truncate(request.getRequestURI(), 255));
        }
        record.setIp(ServletUtils.getIp());
        if (operLog.saveParam()) {
            record.setParam(truncate(argsToString(joinPoint.getArgs()), 2000));
        }
        try {
            Object result = joinPoint.proceed();
            record.setStatus(0);
            return result;
        } catch (Throwable e) {
            record.setStatus(1);
            record.setErrorMsg(truncate(e.getMessage(), 2000));
            throw e;
        } finally {
            record.setCostMs((int) (System.currentTimeMillis() - start));
            try {
                long loginId = StpUtil.getLoginIdAsLong();
                SysUser user = sysUserMapper.selectById(loginId);
                record.setOperName(user == null ? String.valueOf(loginId) : user.getUsername());
            } catch (Exception ignored) {
                // 未登入場景（理論上不會出現在受保護接口）
            }
            operLogService.record(record);
        }
    }

    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (skip(arg)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(arg);
        }
        return sb.toString();
    }

    private boolean skip(Object arg) {
        return arg instanceof MultipartFile
                || arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult;
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}

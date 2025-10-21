package com.baidu.security;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RoleCheckAspect {

    @Around("@annotation(com.baidu.security.RequireRoles) || @within(com.baidu.security.RequireRoles)")
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        RequireRoles ann = method.getAnnotation(RequireRoles.class);
        if (ann == null) {
            ann = pjp.getTarget().getClass().getAnnotation(RequireRoles.class);
        }
        if (ann != null) {
            Role current = RequestContext.getRole();
            boolean allowed = false;
            if (current != null) {
                for (Role r : ann.value()) {
                    if (r == current) { allowed = true; break; }
                }
            }
            if (!allowed) {
                throw new SecurityException("权限不足");
            }
        }
        return pjp.proceed();
    }
}


package com.baidu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("userContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-Role");
        try {
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                try {
                    RequestContext.setUserId(Long.parseLong(userIdHeader));
                } catch (NumberFormatException ignored) {
                    // 忽略无效的用户ID，保持为 null
                }
            }
            RequestContext.setRole(Role.fromHeader(roleHeader));
            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}


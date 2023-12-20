package com.igloosec.smartguard.next.agentmanager.config;

import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

public class SgCommonsRequestLoggingFilter extends CommonsRequestLoggingFilter {
    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        // super.afterRequest(request, message);
    }
}

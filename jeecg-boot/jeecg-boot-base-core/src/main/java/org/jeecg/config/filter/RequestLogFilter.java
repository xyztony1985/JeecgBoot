package org.jeecg.config.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 全局请求日志过滤器
 * 用于在所有请求进入业务逻辑前打印 URL 信息
 */
@Slf4j
@Component
@Order(1) // 确保优先级最高，最先执行
public class RequestLogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        log.info(">>> 请求: {} {}{}", method, uri, queryString != null ? "?" + queryString : "");

        filterChain.doFilter(servletRequest, servletResponse);
    }

}

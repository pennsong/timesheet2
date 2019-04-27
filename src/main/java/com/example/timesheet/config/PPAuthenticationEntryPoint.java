package com.example.timesheet.config;

import com.example.timesheet.util.PPJson;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public final class PPAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException) throws IOException {

        PPJson ppJson = new PPJson();
        ppJson.put("code", "401");
        ppJson.put("message", "未登录或登录失败!");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(ppJson.toString());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }
}

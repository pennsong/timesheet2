package com.example.timesheet.config;

import com.example.timesheet.util.PPJson;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException ex) throws IOException, ServletException {
        PPJson ppJson = new PPJson();
        ppJson.put("code", "403");
        ppJson.put("message", "没有权限!");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(ppJson.toString());
        response.setStatus(200);
    }
}

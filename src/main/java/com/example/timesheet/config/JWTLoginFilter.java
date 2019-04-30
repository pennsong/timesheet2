package com.example.timesheet.config;

import com.example.timesheet.exception.PPValidateException;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {
    static final long EXPIRATIONTIME = 432_000_000;     // 5天
    static final String SECRET = "P@ssw02d";            // JWT密码
    static final String TOKEN_PREFIX = "Bearer";        // Token前缀
    static final String HEADER_STRING = "Authorization";// 存放Token的Header Key

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest req, HttpServletResponse res)
            throws AuthenticationException, IOException, ServletException {

        // JSON反序列化成 AccountCredentials
        AccountCredentials creds = new ObjectMapper().readValue(req.getInputStream(), AccountCredentials.class);

        log.info("pptest creds:" + creds.getUsername() + ", " + creds.getPassword());

        // 返回一个验证令牌
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        creds.getUsername(),
                        creds.getPassword()
                )
        );
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest req,
            HttpServletResponse res, FilterChain chain,
            Authentication auth) {
        addAuthentication(res, ((YongHu) auth.getPrincipal()));
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType("application/json");

        PPJson ppJson = new PPJson();
        ppJson.put("code", "401");
        ppJson.put("message", "没有通过认证!");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print(ppJson.toString());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    private void addAuthentication(HttpServletResponse response, YongHu yongHu) {
        // 生成JWT
        String JWT = Jwts.builder()
//                // 保存权限（角色）
                .claim("authorities", String.join(",", yongHu.getRoles()))
                // 用户名写入标题
                .setSubject(yongHu.getYongHuMing())
                .claim("yongHuId", yongHu.getId())
                .claim("yongHuMing", yongHu.getYongHuMing())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                // 签名设置
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();

        // 将 JWT 写入 body
        try {
            response.setContentType("application/json");

            PPJson ppJson = new PPJson();
            ppJson.put("code", "1");
            ppJson.put("data", JWT);
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(ppJson.toString());
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }
}

class AccountCredentials {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

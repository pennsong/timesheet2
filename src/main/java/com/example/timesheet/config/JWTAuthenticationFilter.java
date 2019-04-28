package com.example.timesheet.config;

import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.util.PPJson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends GenericFilterBean {
    @Autowired
    private YongHuRepository yongHuRepository;

    static final long EXPIRATIONTIME = 432_000_000;     // 5天
    static final String SECRET = "P@ssw02d";            // JWT密码
    static final String TOKEN_PREFIX = "Bearer";        // Token前缀
    static final String HEADER_STRING = "Authorization";// 存放Token的Header Key

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            Authentication authentication = getAuthentication((HttpServletRequest) request);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            PPJson ppJson = new PPJson();
            ppJson.put("code", "400");
            ppJson.put("message", e.getMessage());
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(ppJson.toString());
            ((HttpServletResponse) response).setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    private Authentication getAuthentication(HttpServletRequest request) {
        // 从Header中拿到token
        String token = request.getHeader(HEADER_STRING);

        if (token != null) {
            // 解析 Token
            Claims claims = Jwts.parser()
                    // 验签
                    .setSigningKey(SECRET)
                    // 去掉 Bearer
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody();
            // 拿用户id, 用户名
            Long yongHuId = new Long((String) ("" + claims.get("yongHuId")));
            String yongHuMing = (String) (claims.get("yongHuMing"));

            PPJson ppJson = new PPJson();
            ppJson.put("yongHuId", yongHuId);
            ppJson.put("yongHuMing", yongHuMing);

            // 得到 权限（角色）
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList((String) claims.get("authorities"));



            // 返回验证令牌
            return yongHuId != null ?
                    new UsernamePasswordAuthenticationToken(ppJson, null, authorities) :
                    null;
        }
        return null;
    }
}
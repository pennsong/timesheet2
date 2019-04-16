package com.example.timesheet.service;

import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.YongHuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PPUserDetailsService implements UserDetailsService {
    @Autowired
    private YongHuRepository yongHuRepository;

    @Override
    public UserDetails loadUserByUsername(String yongHuMing) throws UsernameNotFoundException {
         YongHu yongHu = yongHuRepository.findOneByYongHuMing(yongHuMing);
        if (yongHu == null) {
            throw new UsernameNotFoundException(
                    "没有找到此用户: "+ yongHuMing);
        }

        // 主动调用下以触发懒加载
        yongHu.getAuthorities();

        return yongHu;
    }

    private static List<GrantedAuthority> getAuthorities (List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return authorities;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

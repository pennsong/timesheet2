package com.example.timesheet.model;

import com.example.timesheet.util.PPUtil;
import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class YongHu extends PPEntityTypeValidatableAbstract implements UserDetails {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 名称
     */
    @NotBlank
    @Size(min = 2)
    @Column(unique = true)
    private String yongHuMing;

    /**
     * 加密后的密码
     */
    @NotBlank
    @Setter
    @JsonIgnore
    private String jiaMiMiMa;

    /**
     * 小时费用
     */
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Setter
    private BigDecimal xiaoShiFeiYong;

    @ElementCollection
    @NotEmpty
    private List<String> roles;

    @Override
    public String toString() {
        return "用户: " + yongHuMing;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return jiaMiMiMa;
    }

    @Override
    public String getUsername() {
        return yongHuMing;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

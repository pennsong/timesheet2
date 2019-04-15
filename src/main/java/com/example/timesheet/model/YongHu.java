package com.example.timesheet.model;

import com.example.timesheet.util.PPUtil;
import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class YongHu extends PPEntityTypeValidatableAbstract {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 名称
     */
    @NotEmpty
    @Column(unique = true)
    private String yongHuMing;

    /**
     * 加密后的密码
     */
    @NotEmpty
    @Setter
    private String jiaMiMiMa;

    /**
     * 小时费用
     */
    @NotNull
    @Positive
    @Setter
    private BigDecimal xiaoShiFeiYong;

    @Override
    public String toString() {
        return "用户: " + yongHuMing;
    }
}

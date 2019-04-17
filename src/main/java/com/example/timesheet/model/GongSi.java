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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公司
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GongSi extends PPEntityTypeValidatableAbstract {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 名称
     */
    @NotBlank
    @Column(unique = true)
    @Setter
    private String mingCheng;

    /**
     * 结算日
     */
    @Setter
    private LocalDate jieSuanRi;

    /**
     * 如果jieSuanRi为null则返回MIN_DATE
     */
    public LocalDate getJieSuanRi() {
        return jieSuanRi == null ? PPUtil.MIN_DATE : jieSuanRi;
    }

    @Override
    public String toString() {
        return "公司: " + mingCheng;
    }
}

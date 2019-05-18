package com.example.timesheet.model;

import com.example.timesheet.util.PPUtil;
import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    @GeneratedValue(strategy=GenerationType.TABLE, generator = "gongsi")
    @TableGenerator(name = "gongsi", allocationSize = 1, table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "current_val")
    private Long id;

    /**
     * 名称
     */
    @NotBlank
    @Column(unique = true)
    @Setter
    private String mingCheng;

    /**
     * 结算日，结算当天的工作记录一并冻结
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

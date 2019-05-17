package com.example.timesheet.model;

import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 提成
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TiCheng extends PPEntityTypeValidatableAbstract {
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator = "ticheng")
    @TableGenerator(name = "ticheng", allocationSize = 1, table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "current_val")
    private Long id;

    /**
     * 提成的用户
     */
    @ManyToOne(optional = false)
    private YongHu yongHu;

    /**
     * 提成日期
     */
    @NotNull
    private LocalDate riQi;

    /**
     * 提成金额
     */
    @NotNull
    private BigDecimal jinE;

    /**
     * 备注
     */
    private String beiZhu;

    @Override
    public String toString() {
        return "提成: (" + yongHu.getYongHuMing() + ", " + riQi + ", " + jinE + ", " + beiZhu + ")";
    }
}

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
 * 支付
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ZhiFu extends PPEntityTypeValidatableAbstract {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 支付公司
     */
    @ManyToOne(optional = false)
    private GongSi gongSi;

    /**
     * 支付日期
     */
    @NotNull
    private LocalDate riQi;

    /**
     * 支付金额
     */
    @NotNull
    private BigDecimal jinE;

    /**
     * 备注
     */
    private String beiZhu;

    @Override
    public String toString() {
        return "支付: (" + gongSi.getMingCheng() + ", " + riQi + ", " + jinE + ", " + beiZhu + ")";
    }
}

package com.example.timesheet.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 提成标准
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Embeddable
public class TiChengBiaoZhun {
    /**
     * 所属用户
     */
    @NotNull
    @ManyToOne
    private YongHu yongHu;

    /**
     * 开始日期
     */
    @NotNull
    private LocalDate kaiShi;

    /**
     * 小时提成
     */
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal xiaoShiTiCheng;

    @Override
    public String toString() {
        return "提成标准: (" + yongHu.getYongHuMing() + ", " + kaiShi + ", " + xiaoShiTiCheng + ")";
    }
}

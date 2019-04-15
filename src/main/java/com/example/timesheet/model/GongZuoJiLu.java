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
import java.time.LocalDateTime;

/**
 * 工作记录
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GongZuoJiLu extends PPEntityTypeValidatableAbstract {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * 开始时间
     */
    @NotNull
    private LocalDateTime kaiShi;

    /**
     * 结束时间
     */
    @NotNull
    private LocalDateTime jieShu;

    /**
     * 工作人员
     */
    @ManyToOne(optional = false)
    private YongHu yongHu;

    /**
     * 工作项目
     */
    @ManyToOne(optional = false)
    private XiangMu xiangMu;

    /**
     * 工作内容
     */
    @NotEmpty
    private String beiZhu;

    @Override
    public String toString() {
        return "工作记录: (" + kaiShi + ", " + jieShu + ", " + yongHu.getYongHuMing() + ", " + xiangMu.getMingCheng() + ", " + beiZhu + ")";
    }
}

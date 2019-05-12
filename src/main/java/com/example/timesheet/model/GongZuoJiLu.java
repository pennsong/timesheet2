package com.example.timesheet.model;

import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
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
    @NotBlank
    private String beiZhu;

    /**
     * 验证
     * <p>
     * <i>
     * 导入工作记录的结束时间要大于开始时间!
     * </i>
     */
    @Override
    public void validate() {
      	// 结束时间必定大于开始时间
        if (kaiShi.isAfter(jieShu) || kaiShi.isEqual(jieShu)) {
            throw new PPBusinessException("导入工作记录的结束时间要大于开始时间!");
        }
    }

    @Override
    public String toString() {
        return "工作记录: (" + kaiShi + ", " + jieShu + ", " + yongHu.getYongHuMing() + ", " + xiangMu.getMingCheng() + ", " + beiZhu + ")";
    }
}

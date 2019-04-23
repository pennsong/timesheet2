package com.example.timesheet.model;

import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.util.PPUtil;
import com.example.timesheet.validator.PPEntityTypeValidatableAbstract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 项目
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class XiangMu extends PPEntityTypeValidatableAbstract {
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
     * 公司
     */
    @ManyToOne(optional = false)
    @JsonIgnore
    private GongSi gongSi;

    public String getGongSi_mingCheng() {
        return gongSi.getMingCheng();
    }

    /**
     * 计费标准列表
     */
    @ElementCollection
    @Valid
    @OrderBy("kaiShi DESC")
    private List<JiFeiBiaoZhun> jiFeiBiaoZhuns;

    /**
     * 获取计费标准列表副本
     */
    public List<JiFeiBiaoZhun> getJiFeiBiaoZhuns() {
        return jiFeiBiaoZhuns.stream().collect(Collectors.toList());
    }

    /**
     * 添加成员
     * <p>
     * 1) 如果成员已存在, 则抛异常<br>
     * 2) 把用户的小时费用作为成员的默认小时费用<br>
     * 3) MIN_DATE为默认开始时间
     *
     * @param yongHu 成员用户
     */
    public void addChengYuan(YongHu yongHu) {
        // -如果成员已存在, 则抛异常
        if (jiFeiBiaoZhuns.stream().anyMatch(item -> item.getYongHu().getId().compareTo(yongHu.getId()) == 0)) {
            throw new PPBusinessException("此用户已是此项目成员, 不能添加!");
        }
        // -

        JiFeiBiaoZhun jiFeiBiaoZhun = new JiFeiBiaoZhun(yongHu, PPUtil.MIN_DATE, yongHu.getXiaoShiFeiYong());

        jiFeiBiaoZhuns.add(jiFeiBiaoZhun);
    }

    /**
     * 移除成员
     * <p>
     * 1) 如果成员不存在, 则抛异常<br>
     * 2) 如果成员已有工作记录, 则抛异常, 不允许移除<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param yongHu 待移除成员用户
     */
    public void removeChengYuan(YongHu yongHu) {
        List<JiFeiBiaoZhun> result = jiFeiBiaoZhuns.stream().filter(item -> item.getYongHu().getId().compareTo(yongHu.getId()) == 0).collect(Collectors.toList());

        // -如果成员不存在, 则抛异常
        if (result.isEmpty()) {
            throw new PPBusinessException("此用户不是此项目成员, 不能移除!");
        }
        // -

        jiFeiBiaoZhuns.removeAll(result);
    }

    /**
     * 添加计费标准
     * <p>
     * 1) 如果存在此成员的同一日期的计费标准则替换原来的<br>
     * 2) 如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许添加<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param jiFeiBiaoZhun 待添加计费标准
     */
    public void addJiFeiBiaoZhun(JiFeiBiaoZhun jiFeiBiaoZhun) {
        // -如果存在此成员的同一日期的计费标准则替换原来的
        Optional<JiFeiBiaoZhun> existRecord = jiFeiBiaoZhuns
                .stream()
                .filter(
                        item -> (item.getYongHu().getId().compareTo(jiFeiBiaoZhun.getYongHu().getId()) == 0 && item.getKaiShi().isEqual(jiFeiBiaoZhun.getKaiShi()))
                ).findFirst();

        if (existRecord.isPresent()) {
            jiFeiBiaoZhuns.remove(existRecord.get());
        }
        // -

        jiFeiBiaoZhuns.add(jiFeiBiaoZhun);
    }

    /**
     * 移除计费标准
     * <p>
     * 1) 如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许移除<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param yongHu 待移除计费标准所属用户
     * @param kaiShi 待移除计费标准开始日期
     */
    public void removeJiFeiBiaoZhun(YongHu yongHu, LocalDate kaiShi) {
        Optional<JiFeiBiaoZhun> existRecord = jiFeiBiaoZhuns
                .stream()
                .filter(
                        item -> (item.getYongHu().getId().compareTo(yongHu.getId()) == 0 && item.getKaiShi().isEqual(kaiShi))
                ).findFirst();

        if (existRecord.isPresent()) {
            jiFeiBiaoZhuns.remove(existRecord.get());
        } else {
            throw new PPBusinessException("没有找到复合条件的项目计费标准, 无法移除!");
        }
    }

//    @Override
//    public void validate() {
//        if (jiFeiBiaoZhuns == null || jiFeiBiaoZhuns.isEmpty()) {
//            throw new PPBusinessException("计费标准列表至少要有1项!");
//        }
//    }

    @Override
    public String toString() {
        return "项目: " + mingCheng;
    }
}

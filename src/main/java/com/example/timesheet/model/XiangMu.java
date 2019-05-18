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
    @GeneratedValue(strategy=GenerationType.TABLE, generator = "xiangmu")
    @TableGenerator(name = "xiangmu", allocationSize = 1, table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "current_val")
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
    private GongSi gongSi;

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
     * 提成标准列表
     */
    @ElementCollection
    @Valid
    @OrderBy("kaiShi DESC")
    private List<TiChengBiaoZhun> tiChengBiaoZhuns;

    /**
     * 获取提成标准列表副本
     */
    public List<TiChengBiaoZhun> getTiChengBiaoZhuns() {
        return tiChengBiaoZhuns.stream().collect(Collectors.toList());
    }

    /**
     * 添加成员
     * <p>
     * 1) 如果成员已存在, 则抛异常<br>
     * 2) 把用户的小时费用作为成员的默认小时费用<br>
     * 3) 把用户的小时提成作为成员的默认小时提成<br>
     * 4) MIN_DATE为默认开始时间
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
        
        TiChengBiaoZhun tiChengBiaoZhun = new TiChengBiaoZhun(yongHu, PPUtil.MIN_DATE, yongHu.getXiaoShiTiCheng());
        tiChengBiaoZhuns.add(tiChengBiaoZhun);
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
        List<TiChengBiaoZhun> result2 = tiChengBiaoZhuns.stream().filter(item -> item.getYongHu().getId().compareTo(yongHu.getId()) == 0).collect(Collectors.toList());
        
        // -如果成员不存在, 则抛异常
        if (result.isEmpty() || result2.isEmpty()) {
            throw new PPBusinessException("此用户不是此项目成员, 不能移除!");
        }
        // -

        jiFeiBiaoZhuns.removeAll(result);
        tiChengBiaoZhuns.removeAll(result2);
    }

    /**
     * 添加计费标准
     * <p>
     * 1) 如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许添加<br>
     * 2) 如果存在此成员的同一日期的计费标准则替换原来的<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param jiFeiBiaoZhun 待添加计费标准
     */
    public void addJiFeiBiaoZhun(JiFeiBiaoZhun jiFeiBiaoZhun) {
      	// -如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许添加
        if(jiFeiBiaoZhun.getKaiShi().isBefore(gongSi.getJieSuanRi()) || jiFeiBiaoZhun.getKaiShi().isEqual(gongSi.getJieSuanRi())) {
           	throw new PPBusinessException("计费标准开始日期早于或等于公司结算日期，不可添加！");
        }
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
      	// -如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许移除
	    if(kaiShi.isBefore(gongSi.getJieSuanRi()) || kaiShi.isEqual(gongSi.getJieSuanRi())) {
	    	    throw new PPBusinessException("计费标准开始日期早于或等于公司结算日期，不可移除！");
	    }
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
    
    /**
     * 添加提成标准
     * <p>
     * 1) 如果日期早于或等于用户个人结算日, 则抛异常, 不允许添加<br>
     * 2) 如果存在此成员的同一日期的提成标准则替换原来的<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param tiChengBiaoZhun 待添加提成标准
     */
    public void addTiChengBiaoZhun(TiChengBiaoZhun tiChengBiaoZhun) {
    	    // -如果日期早于或等于用户个人的结算日, 则抛异常, 不允许添加
        if(tiChengBiaoZhun.getKaiShi().isBefore(tiChengBiaoZhun.getYongHu().getJieSuanRi()) || tiChengBiaoZhun.getKaiShi().isEqual(tiChengBiaoZhun.getYongHu().getJieSuanRi())) {
           	throw new PPBusinessException("提成标准开始日期早于或等于用户个人结算日期，不可添加！");
        }
        // -如果存在此成员的同一日期的提成标准则替换原来的
        Optional<TiChengBiaoZhun> existRecord = tiChengBiaoZhuns
                .stream()
                .filter(
                        item -> (item.getYongHu().getId().compareTo(tiChengBiaoZhun.getYongHu().getId()) == 0 && item.getKaiShi().isEqual(tiChengBiaoZhun.getKaiShi()))
                ).findFirst();

        if (existRecord.isPresent()) {
            tiChengBiaoZhuns.remove(existRecord.get());
        }
        // -

        tiChengBiaoZhuns.add(tiChengBiaoZhun);
    }

    /**
     * 移除提成标准
     * <p>
     * 1) 如果日期早于或等于用户个人的结算日, 则抛异常, 不允许移除<br>
     * 不过这个检查需要用到多个Aggregator, 所以放到Service里
     *
     * @param yongHu 待移除提成标准所属用户
     * @param kaiShi 待移除提成标准开始日期
     */
    public void removeTiChengBiaoZhun(YongHu yongHu, LocalDate kaiShi) {
      	// -如果日期早于或等于用户个人的结算日, 则抛异常, 不允许移除
	    if(kaiShi.isBefore(yongHu.getJieSuanRi()) || kaiShi.isEqual(yongHu.getJieSuanRi())) {
	    	    throw new PPBusinessException("计费标准开始日期早于或等于用户个人结算日期，不可移除！");
	    }
        Optional<TiChengBiaoZhun> existRecord = tiChengBiaoZhuns
                .stream()
                .filter(
                        item -> (item.getYongHu().getId().compareTo(yongHu.getId()) == 0 && item.getKaiShi().isEqual(kaiShi))
                ).findFirst();

        if (existRecord.isPresent()) {
            tiChengBiaoZhuns.remove(existRecord.get());
        } else {
            throw new PPBusinessException("没有找到复合条件的项目提成标准, 无法移除!");
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

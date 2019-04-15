package com.example.timesheet.service;

import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.exception.PPItemNotExistException;
import com.example.timesheet.model.*;
import com.example.timesheet.repository.*;
import org.json.JSONObject;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class MainService {
    @Autowired
    private YongHuRepository yongHuRepository;

    @Autowired
    private GongSiRepository gongSiRepository;

    @Autowired
    private XiangMuRepository xiangMuRepository;

    @Autowired
    private GongZuoJiLuRepository gongZuoJiLuRepository;

    @Autowired
    private ZhiFuRepository zhiFuRepository;

    private Repositories repositories;

    public MainService(ListableBeanFactory listableBeanFactory) {
        this.repositories = new Repositories(listableBeanFactory);
    }

    // -用户
    // -

    // -公司

    /**
     * 新建公司
     *
     * @param mingCheng 公司名称
     */
    public GongSi createGongSi(String mingCheng) {
        GongSi gongSi = new GongSi(null, mingCheng, null);

        return gongSiRepository.save(gongSi);
    }

    /**
     * 删除公司
     *
     * @param id 公司id
     */
    public void deleteGongSi(Long id) {
        gongSiRepository.deleteById(id);
    }

    /**
     * 修改公司名称
     *
     * @param id        公司id
     * @param mingCheng 公司名称
     */
    public void setGongSiMingCheng(Long id, String mingCheng) {
        GongSi gongSi = gongSiRepository.findById(id).get();
        gongSi.setMingCheng(mingCheng);
    }

    /**
     * 设置公司结算日
     *
     * @param id        公司id
     * @param jieSuanRi 公司结算日
     */
    public void setGongSiJieSuanRi(Long id, LocalDate jieSuanRi) {
        GongSi gongSi = gongSiRepository.findById(id).get();
        gongSi.setJieSuanRi(jieSuanRi);
    }
    // -

    // -项目

    /**
     * 新建项目
     *
     * @param mingCheng 项目名称
     * @param gongSiId  公司id
     */
    public void createXiangMu(String mingCheng, Long gongSiId) {
        GongSi gongSi = gainEntityWithExistsChecking(GongSi.class, gongSiId);
        XiangMu xiangMu = new XiangMu(null, mingCheng, gongSi, new ArrayList<JiFeiBiaoZhun>());

        xiangMuRepository.save(xiangMu);
    }

    /**
     * 删除项目
     *
     * @param id 项目id
     */
    public void deleteXiangMu(Long id) {
        xiangMuRepository.deleteById(id);
    }

    /**
     * 修改项目名称
     */

    /**
     * 添加项目计费标准
     * <p>
     * 1) 如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许添加<br>
     *
     * @param xiangMuId      项目id
     * @param yongHuId       用户id
     * @param kaiShi         开始日期
     * @param xiaoShiFeiYong 小时费用
     */
    public void addXiangMuJiFeiBiaoZhun(Long xiangMuId, Long yongHuId, LocalDate kaiShi, BigDecimal xiaoShiFeiYong) {
        XiangMu xiangMu = gainEntityWithExistsChecking(XiangMu.class, xiangMuId);
        GongSi gongSi = gainEntityWithExistsChecking(GongSi.class, xiangMu.getGongSi().getId());
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, yongHuId);

        // --如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许添加
        if (kaiShi.isBefore(gongSi.getJieSuanRi()) || kaiShi.isEqual(gongSi.getJieSuanRi())) {
            throw new PPBusinessException("计费标准开始日期早于或等于项目所属公司的结算日, 不允许添加!");
        }
        // --

        xiangMu.addJiFeiBiaoZhun(new JiFeiBiaoZhun(yongHu, kaiShi, xiaoShiFeiYong));
    }

    /**
     * 移除项目计费标准
     * <p>
     * 1) 如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许移除<br>
     *
     * @param xiangMuId 项目id
     * @param yongHuId  用户id
     * @param kaiShi    开始日期
     */
    public void removeXiangMuJiFeiBiaoZhun(Long xiangMuId, Long yongHuId, LocalDate kaiShi) {
        XiangMu xiangMu = gainEntityWithExistsChecking(XiangMu.class, xiangMuId);
        GongSi gongSi = gainEntityWithExistsChecking(GongSi.class, xiangMu.getGongSi().getId());
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, yongHuId);

        // -如果日期早于或等于项目所属公司的结算日, 则抛异常, 不允许移除
        if (kaiShi.isBefore(gongSi.getJieSuanRi()) || kaiShi.isEqual(gongSi.getJieSuanRi())) {
            throw new PPBusinessException("计费标准开始日期早于或等于项目所属公司的结算日, 不允许移除!");
        }
        // -

        xiangMu.removeJiFeiBiaoZhun(yongHu, kaiShi);
    }

    /**
     * 添加项目成员
     * <p>
     * 1) 如果成员已有工作记录, 则抛异常, 不允许添加<br>
     *
     * @param xiangMuId 项目id
     * @param yongHuId  成员的用户id
     */
    public void addXiangMuChengYuan(Long xiangMuId, Long yongHuId) {
        XiangMu xiangMu = gainEntityWithExistsChecking(XiangMu.class, xiangMuId);
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, yongHuId);

        xiangMu.addChengYuan(yongHu);
    }

    /**
     * 移除项目成员
     * <p>
     * 1) 如果成员已有工作记录, 则抛异常, 不允许移除<br>
     *
     * @param xiangMuId 项目id
     * @param yongHuId  成员的用户id
     */
    public void removeXiangMuChengYuan(Long xiangMuId, Long yongHuId) {
        XiangMu xiangMu = gainEntityWithExistsChecking(XiangMu.class, xiangMuId);
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, yongHuId);

        // --如果成员已有工作记录, 则抛异常, 不允许移除
        if (gongZuoJiLuRepository.findByYongHuAndXiangMu(yongHuId, xiangMuId)) {
            throw new PPBusinessException("此用户在此项目已有工作记录, 不能移除!");
        }
        // --

        xiangMu.removeChengYuan(yongHu);
    }
    // -

    // -工作记录

    /**
     * 新建工作记录
     * <p>
     * 1) 项目没有用户的计费标准, 抛异常, 不允许添加<br>
     * 2) 工作记录的时间早于或等于项目所属公司的计算日, 抛异常, 不允许添加<br>
     * 3) 如工作记录跨越24:00则拆分成以所属日期为粒度的多条记录
     *
     * @param yongHuMing       用户名
     * @param xiangMuMingCheng 项目名称
     * @param kaiShi           工作记录开始时间
     * @param jieShu           工作记录结束时间
     */
    public void createGongZuoJiLu(String yongHuMing, String xiangMuMingCheng, LocalDateTime kaiShi, LocalDateTime jieShu, String beiZhu) {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing(yongHuMing);
        if (yongHu == null) {
            throw new PPItemNotExistException("指定用户不存在!");
        }

        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng(xiangMuMingCheng);
        if (xiangMu == null) {
            throw new PPItemNotExistException("指定项目不存在!");
        }

        // --项目没有用户的计费标准, 抛异常, 不允许添加
        if (xiangMu.getJiFeiBiaoZhuns().stream().noneMatch(item -> item.getYongHu().getYongHuMing().equals(yongHuMing))) {
            throw new PPBusinessException("项目没有用户的计费标准, 不允许添加!");
        }
        // --

        // --工作记录的时间早于或等于项目所属公司的计算日, 抛异常, 不允许添加
        LocalDateTime dateTime = xiangMu.getGongSi().getJieSuanRi().plusDays(1).atStartOfDay();
        if (jieShu.isBefore(dateTime)) {
            throw new PPBusinessException("工作记录的时间早于或等于项目所属公司的计算日, 不允许添加!");
        }
        // --

        // --如工作记录跨越24:00则拆分成以所属日期为粒度的多条记录
        List<GongZuoJiLu> gongZuoJiLus = new ArrayList<>();
        Long days = DAYS.between(kaiShi.toLocalDate(), jieShu.toLocalDate());
        switch (days.intValue()) {
            case 0:
                gongZuoJiLus.add(new GongZuoJiLu(null, kaiShi, jieShu, yongHu, xiangMu, beiZhu));
                break;
            case 1:
                gongZuoJiLus.add(new GongZuoJiLu(null, kaiShi, kaiShi.toLocalDate().plusDays(1).atStartOfDay().minusSeconds(1), yongHu, xiangMu, beiZhu));
                gongZuoJiLus.add(new GongZuoJiLu(null, kaiShi.toLocalDate().plusDays(1).atStartOfDay(), jieShu, yongHu, xiangMu, beiZhu));
                break;
            default:
                gongZuoJiLus.add(new GongZuoJiLu(null, kaiShi, kaiShi.toLocalDate().plusDays(1).atStartOfDay().minusSeconds(1), yongHu, xiangMu, beiZhu));
                for (int i = 1; i <= days - 1; i++) {
                    gongZuoJiLus.add(new GongZuoJiLu(null, kaiShi.toLocalDate().plusDays(i).atStartOfDay(), kaiShi.toLocalDate().plusDays(i + 1).atStartOfDay().minusSeconds(1), yongHu, xiangMu, beiZhu));
                }
                gongZuoJiLus.add(new GongZuoJiLu(null, jieShu.toLocalDate().atStartOfDay(), jieShu, yongHu, xiangMu, beiZhu));
                break;
        }
        // --

        gongZuoJiLuRepository.saveAll(gongZuoJiLus);
    }

    /**
     * 删除工作记录
     *
     * @param id 工作记录id
     */
    public void deleteGongZuoJiLu(Long id) {
        gongZuoJiLuRepository.deleteById(id);
    }
    // -

    // -支付

    /**
     * 新建支付
     * <p>
     * 需要满足以下条件:
     * 1) 时间不能早于或等于公司的结算日
     *
     * @param gongSiMingCheng
     * @param riQi
     * @param jinE
     * @param beiZhu
     */
    public ZhiFu createZhiFu(String gongSiMingCheng, LocalDate riQi, BigDecimal jinE, String beiZhu) {
        GongSi gongSi = gongSiRepository.findOneByMingCheng(gongSiMingCheng);

        // --时间不能早于或等于公司的结算日
        if (riQi.isBefore(gongSi.getJieSuanRi().plusDays(1))) {
            throw new PPBusinessException("支付时间不能早于或等于公司的结算日!");
        }
        // --

        if (gongSi == null) {
            throw new PPItemNotExistException("没有找到对应公司!");
        }
        ZhiFu zhiFu = new ZhiFu(null, gongSi, riQi, jinE, beiZhu);

        return zhiFuRepository.save(zhiFu);
    }

    /**
     * 删除支付
     * <p>
     * 1) 时间不能早于或等于公司的结算日
     *
     * @param id 支付id
     */
    public void deleteZhiFu(Long id) {
        zhiFuRepository.deleteById(id);
    }
    // -

    // -报告

    /**
     * 生成报告
     *
     * @param gongSiId 公司id
     * @param kaiShi   开始日期
     * @param jieShu   结束日期
     */
    public JSONObject generateBaoGao(Long gongSiId, LocalDate kaiShi, LocalDate jieShu) {
        JSONObject jsonObject = new JSONObject();

        return jsonObject;
    }
    // -

    /**
     * 检查Entity是否存在
     *
     * @param tClass 实体所属类.class
     * @param id     实体id
     */
    public <T> T gainEntityWithExistsChecking(Class<T> tClass, Object id) {
        Optional repositoryObject = repositories.getRepositoryFor(tClass);

        if (!(repositoryObject.isPresent())) {
            throw new PPItemNotExistException("操作的记录所属的Repository不存在!");
        }

        CrudRepository crudRepository = (CrudRepository) repositoryObject.get();

        Optional<T> t = crudRepository.findById(id);
        if (!(t.isPresent())) {
            throw new PPItemNotExistException("操作的记录不存在!");
        } else {
            return t.get();
        }
    }
}

package com.example.timesheet.service;

import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.exception.PPItemNotExistException;
import com.example.timesheet.model.*;
import com.example.timesheet.repository.*;
import com.example.timesheet.util.PPUtil;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
@Transactional
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

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Repositories repositories;

    public MainService(ListableBeanFactory listableBeanFactory) {
        this.repositories = new Repositories(listableBeanFactory);
    }

    // -用户

    // todo 测试

    /**
     * 查询用户
     * <p>
     *
     * @param size 每页记录数
     * @param page 页码
     */
    public Page<YongHu> queryYongHu(Integer size, Integer page) {
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        QYongHu qYongHu = QYongHu.yongHu;
        JPAQuery<YongHu> jpaQuery = factory.select(qYongHu)
                .from(qYongHu)
                .where(predicate);

        jpaQuery.orderBy(qYongHu.yongHuMing.asc());

        return PPUtil.getPageResult(jpaQuery, size, page);
    }

    /**
     * 新建用户
     *
     * @param yongHuMing     用户名
     * @param password       密码
     * @param xiaoShiFeiYong 小时费用
     */
    public YongHu createYongHu(String yongHuMing, String password, BigDecimal xiaoShiFeiYong) {
        YongHu yongHu = new YongHu(null, yongHuMing, passwordEncoder.encode(password), xiaoShiFeiYong, Arrays.asList("USER"));

        return yongHuRepository.save(yongHu);
    }

    /**
     * 删除用户
     * <p>
     * 1) Admin不允许删除
     *
     * @param id 用户id
     */
    public void deleteYongHu(Long id) {
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, id);
        if (yongHu.getYongHuMing().equals("Admin")) {
            throw new PPBusinessException("不允许删除管理员!");
        }

        yongHuRepository.deleteById(id);
    }

    /**
     * 修改用户密码
     *
     * @param id          用户id
     * @param newPassword 新密码
     */
    public void changePassword(Long id, String newPassword) {
        YongHu yongHu = gainEntityWithExistsChecking(YongHu.class, id);

        yongHu.setJiaMiMiMa(passwordEncoder.encode(newPassword));
    }
    // -

    // -公司

    // todo 用querydsl改写

    /**
     * 查询公司
     */
    public Page<GongSi> queryGongSi(Integer size, Integer page) {
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        QGongSi qGongSi = QGongSi.gongSi;
        JPAQuery<GongSi> jpaQuery = factory.select(qGongSi)
                .from(qGongSi)
                .where(predicate);

        jpaQuery.orderBy(qGongSi.mingCheng.asc());

        return PPUtil.getPageResult(jpaQuery, size, page);
    }

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
     * 查询项目
     * <p>
     *
     * @param size 每页记录数
     * @param page 页码
     */
    public Page<XiangMu> queryXiangMu(Integer size, Integer page) {
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();

        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        QXiangMu qXiangMu = QXiangMu.xiangMu;
        QGongSi qGongSi = QGongSi.gongSi;

        JPAQuery<XiangMu> jpaQuery = factory.select(qXiangMu)
                .from(qXiangMu)
                .join(qGongSi)
                .on(qXiangMu.gongSi.id.eq(qGongSi.id))
                .where(predicate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(qXiangMu.gongSi.mingCheng.toString()).and(Sort.by(qXiangMu.mingCheng.toString())));

        jpaQuery.orderBy(qXiangMu.gongSi.mingCheng.asc())
                .orderBy(qXiangMu.mingCheng.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        QueryResults<XiangMu> queryResults = jpaQuery.fetchResults();

        Page<XiangMu> result = new PageImpl(queryResults.getResults(), pageable, queryResults.getTotal());

        return result;
    }

    /**
     * 新建项目
     *
     * @param mingCheng 项目名称
     * @param gongSiId  公司id
     */
    public XiangMu createXiangMu(String mingCheng, Long gongSiId) {
        GongSi gongSi = gainEntityWithExistsChecking(GongSi.class, gongSiId);
        XiangMu xiangMu = new XiangMu(null, mingCheng, gongSi, new ArrayList<JiFeiBiaoZhun>());

        return xiangMuRepository.save(xiangMu);
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
    public void setXiangMuMingCheng(Long id, String mingCheng) {
        XiangMu xiangMu = gainEntityWithExistsChecking(XiangMu.class, id);
        xiangMu.setMingCheng(mingCheng);
    }

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
     * 2) 工作记录的时间早于或等于项目所属公司的结算日, 抛异常, 不允许添加<br>
     * 3) 如工作记录跨越24:00则拆分成以所属日期为粒度的多条记录<br>
     * 4) 如工作记录时间段有重叠, 抛异常, 不允许添加
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

        // --
        Long overLapedGongZuoJiLuCount = gongZuoJiLuRepository.findByOverlapWorkRecords(yongHu.getId(), kaiShi, jieShu);
        if (overLapedGongZuoJiLuCount > 0) {
            throw new PPBusinessException("工作记录时间段有重叠, 不允许添加!");
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
     * <p>
     * 1) 如工作记录时间小于等于公司结算日, 则抛异常, 不允许删除
     *
     * @param id 工作记录id
     */
    public void deleteGongZuoJiLu(Long id) {
        GongZuoJiLu gongZuoJiLu = gainEntityWithExistsChecking(GongZuoJiLu.class, id);

        // 如工作记录时间小于等于公司结算日, 则抛异常, 不允许删除
        LocalDateTime kaishi = gongZuoJiLu.getKaiShi();
        LocalDate jieSuanRi = gongZuoJiLu.getXiangMu().getGongSi().getJieSuanRi();

        if (kaishi.toLocalDate().isEqual(jieSuanRi) || kaishi.toLocalDate().isBefore(jieSuanRi)) {
            throw new PPBusinessException("工作记录时间小于等于公司结算日, 不允许删除!");
        }

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

        if (gongSi == null) {
            throw new PPItemNotExistException("没有找到对应公司!");
        }

        // --时间不能早于或等于公司的结算日
        if (riQi.isBefore(gongSi.getJieSuanRi().plusDays(1))) {
            throw new PPBusinessException("支付时间不能早于或等于公司的结算日!");
        }
        // --

        ZhiFu zhiFu = new ZhiFu(null, gongSi, riQi, jinE, beiZhu);

        return zhiFuRepository.save(zhiFu);
    }

    /**
     * 删除支付
     * <p>
     * 1) 如时间小于等于公司结算日, 则抛异常, 不允许删除
     *
     * @param id 支付id
     */
    public void deleteZhiFu(Long id) {
        ZhiFu zhiFu = gainEntityWithExistsChecking(ZhiFu.class, id);

        // 如时间小于等于公司结算日, 则抛异常, 不允许删除
        LocalDate zhiFuRiQi = zhiFu.getRiQi();
        LocalDate jieSuanRi = zhiFu.getGongSi().getJieSuanRi();

        if (zhiFuRiQi.isEqual(jieSuanRi) || zhiFuRiQi.isBefore(jieSuanRi)) {
            throw new PPBusinessException("支付时间小于等于公司结算日, 不允许删除!");
        }

        zhiFuRepository.deleteById(id);
    }
    // -

    /**
     * 查询工作记录
     * <p>
     *
     * @param yongHuId 用户id
     * @param kaiShi   开始日期(包含)
     * @param jieShu   结束日期(不包含)
     */
    public List<GongZuoJiLu> queryGongZuoJiLu(Long yongHuId, LocalDateTime kaiShi, LocalDateTime jieShu, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("kaiShi").ascending());

        return gongZuoJiLuRepository.findYongHuGongZuoJiLu(yongHuId, kaiShi, jieShu, pageable);
    }

    // todo 测试案例

    /**
     * 查询工作记录
     * <p>
     *
     * @param yongHuId 用户id
     * @param kaiShi   开始日期(包含)
     * @param jieShu   结束日期(不包含)
     * @param size     每页记录数
     * @param page     页码
     */
    public Page<GongZuoJiLu> queryGongZuoJiLu(Long yongHuId, Long gongSiId, LocalDateTime kaiShi, LocalDateTime jieShu, Integer size, Integer page) {
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        JPAQueryFactory factory = new JPAQueryFactory(entityManager);

        QGongZuoJiLu qGongZuoJiLu = QGongZuoJiLu.gongZuoJiLu;

        predicate = predicate.and(
                qGongZuoJiLu.kaiShi.eq(kaiShi).or(qGongZuoJiLu.kaiShi.after(kaiShi))
        );

        predicate = predicate.and(qGongZuoJiLu.jieShu.before(jieShu));

        if (yongHuId != null) {
            predicate = predicate.and(qGongZuoJiLu.yongHu.id.eq(yongHuId));
        }

        if (gongSiId != null) {
            predicate = predicate.and(qGongZuoJiLu.xiangMu.gongSi.id.eq(gongSiId));
        }

        JPAQuery<GongZuoJiLu> jpaQuery = factory.select(qGongZuoJiLu)
                .from(qGongZuoJiLu)
                .where(predicate);

        jpaQuery.orderBy(qGongZuoJiLu.kaiShi.desc());

        return PPUtil.getPageResult(jpaQuery, size, page);
    }

    // -报告

    /**
     * 生成报告
     *
     * @param gongSiId 公司id
     * @param kaiShi   开始日期
     * @param jieShu   结束日期
     */
    public JSONObject generateBaoGao(Long gongSiId, LocalDate kaiShi, LocalDate jieShu) throws JSONException {
        // --查出结束日期前指定公司相关的工作记录和对应费用
        JSONArray gongZuoJiLusJsonArray = new JSONArray();

        List<GongZuoJiLu> gongZuoJiLus = gongZuoJiLuRepository.findGongSiGongZuoJiLu(gongSiId, jieShu.plusDays(1).atStartOfDay());

        // 开始日期消费总额
        BigDecimal kaiShiCostTotal = new BigDecimal("0");

        // 结束日期消费总额
        BigDecimal jieShuCostTotal = new BigDecimal("0");

        for (GongZuoJiLu gongZuoJiLu : gongZuoJiLus) {
            JSONObject jsonObject = new JSONObject();
            XiangMu xiangMu = gongZuoJiLu.getXiangMu();
            List<JiFeiBiaoZhun> jiFeiBiaoZhuns = xiangMu.getJiFeiBiaoZhuns();

            Optional<JiFeiBiaoZhun> optionalJiFeiBiaoZhun = jiFeiBiaoZhuns.stream().filter(
                    item -> item.getYongHu().getId().compareTo(gongZuoJiLu.getYongHu().getId()) == 0
                            && item.getKaiShi().isBefore(gongZuoJiLu.getKaiShi().toLocalDate().plusDays(1))
            ).findFirst();

            if (!optionalJiFeiBiaoZhun.isPresent()) {
                throw new PPBusinessException(gongZuoJiLu.toString() + ": 没有找到计费标准!");
            }

            BigDecimal xiaoShiFeiYong = optionalJiFeiBiaoZhun.get().getXiaoShiFeiYong();
            BigDecimal secondCost = xiaoShiFeiYong.divide(new BigDecimal("3600"), MathContext.DECIMAL128);

            Duration duration = Duration.between(gongZuoJiLu.getKaiShi(), gongZuoJiLu.getJieShu());
            BigDecimal cost = secondCost.multiply(new BigDecimal("" + duration.getSeconds()));

            if (gongZuoJiLu.getKaiShi().isBefore(kaiShi.atStartOfDay())) {
                kaiShiCostTotal = kaiShiCostTotal.add(cost);
            }

            jieShuCostTotal = jieShuCostTotal.add(cost);

            if (
                    (
                            gongZuoJiLu.getKaiShi().toLocalDate().isEqual(kaiShi) || gongZuoJiLu.getKaiShi().toLocalDate().isAfter(kaiShi)
                    ) && (
                            gongZuoJiLu.getKaiShi().toLocalDate().isEqual(jieShu) || gongZuoJiLu.getKaiShi().toLocalDate().isBefore(jieShu)
                    )

            ) {
                jsonObject.put("开始", gongZuoJiLu.getKaiShi());
                jsonObject.put("结束", gongZuoJiLu.getJieShu());
                jsonObject.put("项目", gongZuoJiLu.getXiangMu().getMingCheng());
                jsonObject.put("人员", gongZuoJiLu.getYongHu().getYongHuMing());
                jsonObject.put("耗时", (new BigDecimal("" + duration.getSeconds())).divide(new BigDecimal("" + 3600), MathContext.DECIMAL128));
                jsonObject.put("小时费用", xiaoShiFeiYong);
                jsonObject.put("费用", cost);

                gongZuoJiLusJsonArray.put(jsonObject);
            }
        }
        // --

        // --查出时间段内指定公司相关的支付
        JSONArray zhiFusJsonArray = new JSONArray();

        List<ZhiFu> zhiFus = zhiFuRepository.findGongSiZhiFu(gongSiId, kaiShi, jieShu.plusDays(1));

        for (ZhiFu zhifu : zhiFus) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("日期", zhifu.getRiQi());
            jsonObject.put("金额", zhifu.getJingE());
            jsonObject.put("备注", zhifu.getBeiZhu());

            zhiFusJsonArray.put(jsonObject);
        }

        // --

        // --查出开始时公司Balance
        BigDecimal kaiShiIncoming = zhiFuRepository.calIncomingTotal(gongSiId, kaiShi);
        BigDecimal kaiShiBalance = kaiShiIncoming.subtract(kaiShiCostTotal);
        // --

        // --查出结束时公司Balance
        BigDecimal jieShuIncoming = zhiFuRepository.calIncomingTotal(gongSiId, jieShu);
        BigDecimal jieShuBalance = jieShuIncoming.subtract(jieShuCostTotal);
        // --

        JSONObject reportJsonObject = new JSONObject();
        reportJsonObject.put("开始", kaiShi);
        reportJsonObject.put("结束", jieShu);
        reportJsonObject.put("期初Balance", kaiShiBalance);
        reportJsonObject.put("期末Balance", jieShuBalance);
        reportJsonObject.put("消费记录", gongZuoJiLusJsonArray);
        reportJsonObject.put("充值记录", zhiFusJsonArray);

        return reportJsonObject;
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

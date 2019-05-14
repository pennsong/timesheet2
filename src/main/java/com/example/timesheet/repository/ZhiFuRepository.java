package com.example.timesheet.repository;

import com.example.timesheet.model.GongZuoJiLu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.model.ZhiFu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ZhiFuRepository extends CrudRepository<ZhiFu, Long> {
	/**
     * 指定时间段内的支付：支付的日期 >= 开始日期 && 支付的日期 < 结束日期
     *
     * @param gongSiId 公司id
     * @param kaiShi   开始日期 大于等于
     * @param jieShu   结束日期 小于
     */
    @Query("select " +
            "z " +
            "from ZhiFu z " +
            "join z.gongSi g " +
            "where g.id = :gongSiId " +
            "and z.riQi >= :kaiShi " +
            "and z.riQi < :jieShu " +
            "order by z.riQi")
    List<ZhiFu> findGongSiZhiFu(@Param("gongSiId") Long gongSiId, @Param("kaiShi") LocalDate kaiShi, @Param("jieShu") LocalDate jieShu);

    /**
     * 结算到指定日期的指定公司的收入总和：支付的日期 < 结束日期
     *
     * @param gongSiId 公司id
     * @param jieShu   结束日期 小于
     */
    @Query("select case when sum(z.jinE) is null then 0 else sum(z.jinE) end " +
            "from ZhiFu z " +
            "join z.gongSi g " +
            "where g.id = :gongSiId " +
            "and z.riQi < :jieShu")
    BigDecimal calIncomingTotal(@Param("gongSiId") Long gongSiId, @Param("jieShu") LocalDate jieShu);
}

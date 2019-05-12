package com.example.timesheet.repository;

import com.example.timesheet.model.GongZuoJiLu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.model.TiCheng;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TiChengRepository extends CrudRepository<TiCheng, Long> {
	/**
     * 指定时间段内[开始日期，结束日期)的提成： 提成的日期 >= 开始日期 && 提成的日期 < 结束日期
     *
     * @param yongHuId 用户id
     * @param kaiShi   开始日期 大于等于
     * @param jieShu   结束日期 小于
     */
    @Query("select " +
            "t " +
            "from TiCheng t " +
            "join t.yongHu y " +
            "where y.id = :yongHuId " +
            "and t.riQi >= :kaiShi " +
            "and t.riQi < :jieShu " +
            "order by t.riQi")
    List<TiCheng> findYongHuTiCheng(@Param("yongHuId") Long yongHuId, @Param("kaiShi") LocalDate kaiShi, @Param("jieShu") LocalDate jieShu);

    /**
     * 结算到指定日期的指定用户的提成总和：提成的日期 < 结束日期
     *
     * @param yongHuId 用户id
     * @param jieShu   结束日期 小于
     */
    @Query("select case when sum(t.jinE) is null then 0 else sum(t.jinE) end " +
            "from TiCheng t " +
            "join t.yongHu y " +
            "where y.id = :yongHuId " +
            "and t.riQi < :jieShu")
    BigDecimal calTiChengTotal(@Param("yongHuId") Long yongHuId, @Param("jieShu") LocalDate jieShu);
}

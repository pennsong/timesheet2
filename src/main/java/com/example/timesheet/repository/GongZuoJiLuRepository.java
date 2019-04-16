package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.GongZuoJiLu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GongZuoJiLuRepository extends CrudRepository<GongZuoJiLu, Long> {
    /**
     * 指定用户是否有在指定项目上的工作记录
     *
     * @param yongHuId 用户id
     * @param xiangMuId 项目id
     */
    @Query("select " +
            "case when count(g) > 0 then true else false end " +
            "from GongZuoJiLu g " +
            "join g.yongHu y " +
            "join g.xiangMu x " +
            "where y.id = :yongHuId " +
            "and x.id = :xiangMuId")
    Boolean findByYongHuAndXiangMu(@Param("yongHuId") Long yongHuId, @Param("xiangMuId") Long xiangMuId);

    /**
     * 指定时间段内的工作记录
     *
     * @param gongSiId 公司id
     * @param jieShu 结束日期时间 小于
     */
    @Query("select " +
            "g " +
            "from GongZuoJiLu g " +
            "join g.xiangMu x " +
            "join x.gongSi gs " +
            "where gs.id = :gongSiId " +
            "and g.kaiShi < :jieShu " +
            "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findGongSiGongZuoJiLu(@Param("gongSiId") Long gongSiId, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 指定时间段内的工作记录
     *
     * @param gongSiId 公司id
     * @param kaiShi 开始日期时间 大于等于
     * @param jieShu 结束日期时间 小于
     */
    @Query("select " +
            "g " +
            "from GongZuoJiLu g " +
            "join g.xiangMu x " +
            "join x.gongSi gs " +
            "where gs.id = :gongSiId " +
            "and g.kaiShi >= :kaiShi " +
            "and g.kaiShi < :jieShu " +
            "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findGongSiGongZuoJiLu(@Param("gongSiId") Long gongSiId, @Param("kaiShi") LocalDateTime kaiShi, @Param("jieShu") LocalDateTime jieShu);
}

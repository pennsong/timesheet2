package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.GongZuoJiLu;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GongZuoJiLuRepository extends PagingAndSortingRepository<GongZuoJiLu, Long> {
	/**
     * 指定用户是否有在指定项目上的工作记录
     *
     * @param yongHuId  用户id
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
     * 指定公司截止到结束日期的工作记录：工作记录的结束 <= 结束日期
     *
     * @param gongSiId 公司id
     * @param jieShu   结束日期时间 小于等于
     */
    @Query("select " +
            "g " +
            "from GongZuoJiLu g " +
            "join g.xiangMu x " +
            "join x.gongSi gs " +
            "where gs.id = :gongSiId " +
            "and g.jieShu <= :jieShu " +
            "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findGongSiGongZuoJiLu(@Param("gongSiId") Long gongSiId, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 指定公司指定时间段内的工作记录：工作记录的开始 >= 开始日期 && 工作记录的结束 <= 结束日期
     *
     * @param gongSiId 公司id
     * @param kaiShi   开始日期时间 大于等于
     * @param jieShu   结束日期时间 小于等于
     */
    @Query("select " +
            "g " +
            "from GongZuoJiLu g " +
            "join g.xiangMu x " +
            "join x.gongSi gs " +
            "where gs.id = :gongSiId " +
            "and g.kaiShi >= :kaiShi " +
            "and g.jieShu <= :jieShu " +
            "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findGongSiGongZuoJiLu_Err(@Param("gongSiId") Long gongSiId, @Param("kaiShi") LocalDateTime kaiShi, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 指定人员的指定时间段[开始时间, 结束时间]完整包含的工作记录，片段工作记录不算，工作记录的结束时间=jieShu的算：工作记录的开始 >= 开始日期 && 工作记录的结束 <= 结束日期
     *
     * @param yongHuId 用户id
     * @param kaiShi   开始日期时间 大于等于
     * @param jieShu   结束日期时间 小于等于
     */
    @Query("select " +
            "g " +
            "from GongZuoJiLu g " +
            "join g.yongHu y " +
            "where y.id = :yongHuId " +
            "and g.kaiShi >= :kaiShi " +
            "and g.jieShu <= :jieShu " +
            "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findYongHuGongZuoJiLu(@Param("yongHuId") Long yongHuId, @Param("kaiShi") LocalDateTime kaiShi, @Param("jieShu") LocalDateTime jieShu, Pageable pageable);
    
    /**
     * 指定人员的某结束时间前的完整包含的工作记录，片段工作记录不算，工作记录的结束时间=jieShu的算：工作记录的结束 <= 结束日期
     * @param yongHuId 用户id
     * @param jieShu   结束日期时间 小于等于
     */
    @Query("select "
    		+ "g "
    		+ "from GongZuoJiLu g "
    		+ "join g.yongHu y "
    		+ "where y.id = :yongHuId "
    		+ "and g.jieShu <= :jieShu "
    		+ "order by g.xiangMu, g.kaiShi")
    List<GongZuoJiLu> findYongHuBaoGaoGongZuoJiLu(@Param("yongHuId") Long yongHuId, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 指定人员的指定时间段的指定项目的重叠的工作记录：工作记录的开始 < 结束日期 && 工作记录的结束 > 开始日期
     *
     * @param yongHuId 用户id
     * @param xiangMuIds 项目id数组
     * @param kaiShi   开始日期时间 大于
     * @param jieShu   结束日期时间 小于
     */
    @Query("select count(g) from " +
            "GongZuoJiLu g " +
            "join g.yongHu y " +
            "join g.xiangMu x " +
            "where y.id = :yongHuId " +
            "and " +
            "x.id in :xiangMuIds " +
            "and " +
            "g.jieShu > :kaiShi " +
            "and " +
            "g.kaiShi < :jieShu")
    Long findByOverlapWorkRecordsWithinXiangMus(@Param("yongHuId") Long yongHuId, @Param("xiangMuIds") Long[] xiangMuIds, @Param("kaiShi") LocalDateTime kaiShi, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 指定人员的指定时间段重叠的工作记录：工作记录的开始 < 结束日期 && 工作记录的结束 > 开始日期
     *
     * @param yongHuId 用户id
     * @param kaiShi   开始日期时间 大于
     * @param jieShu   结束日期时间 小于
     */
    @Query("select count(g) from " +
            "GongZuoJiLu g " +
            "join g.yongHu y " +
            "where y.id = :yongHuId " +
            "and " +
            "g.jieShu > :kaiShi " +
            "and " +
            "g.kaiShi < :jieShu")
    Long findByOverlapWorkRecords(@Param("yongHuId") Long yongHuId, @Param("kaiShi") LocalDateTime kaiShi, @Param("jieShu") LocalDateTime jieShu);

    /**
     * 根据备注查找单条工作记录(给测试程序用)
     *
     * @param beiZhu 备注
     */
    GongZuoJiLu findOneByBeiZhu(String beiZhu);
}

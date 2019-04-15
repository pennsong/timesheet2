package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.GongZuoJiLu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface GongZuoJiLuRepository extends CrudRepository<GongZuoJiLu, Long> {
    /**
     *
     */
    @Query("select " +
            "case when count(g) > 0 then true else false end " +
            "from GongZuoJiLu g " +
            "join g.yongHu y " +
            "join g.xiangMu x " +
            "where y.id = :yongHuId " +
            "and x.id = :xiangMuId")
    Boolean findByYongHuAndXiangMu(@Param("yongHuId") Long yongHuId, @Param("xiangMuId") Long xiangMuId);
}

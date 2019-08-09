package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface XiangMuRepository extends CrudRepository<XiangMu, Long> {
    XiangMu findOneByMingCheng(String mingCheng);

    /**
     * 获取所有在某公司旗下的项目id
     * @param gongSiId
     * @return
     */
    @Query("select x.id from " +
            "XiangMu x " +
            "join x.gongSi g " +
            "where g.id = :gongSiId ")
    Long[] findAllIdBelongGongSi(Long gongSiId);
}

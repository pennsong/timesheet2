package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import org.springframework.data.repository.CrudRepository;

public interface XiangMuRepository extends CrudRepository<XiangMu, Long> {
    XiangMu findOneByMingCheng(String mingCheng);
}

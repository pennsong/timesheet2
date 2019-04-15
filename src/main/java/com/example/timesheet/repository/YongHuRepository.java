package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.YongHu;
import org.springframework.data.repository.CrudRepository;

public interface YongHuRepository extends CrudRepository<YongHu, Long> {
    YongHu findOneByYongHuMing(String yongHuMing);
}

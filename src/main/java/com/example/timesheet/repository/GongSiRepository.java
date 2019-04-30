package com.example.timesheet.repository;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GongSiRepository extends CrudRepository<GongSi, Long> {
    GongSi findOneByMingCheng(String mingCheng);

    List<GongSi> findAll(Pageable pageable);

    @Override
    long count();
}

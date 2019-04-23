package com.example.timesheet.repository;

import com.example.timesheet.model.GongZuoJiLu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GongZuoJiLuSpecRepository extends JpaSpecificationExecutor<GongZuoJiLu> {
}

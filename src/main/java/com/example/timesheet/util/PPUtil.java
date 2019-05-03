package com.example.timesheet.util;

import com.example.timesheet.exception.PPValidateException;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class PPUtil {
    public static LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    public static LocalDate MAX_DATE = LocalDate.of(2900, 12, 31);

    public static void throwConstraintViolationsAsString(Set<ConstraintViolation<Object>> constraintViolations) {
        List<String> errors = new ArrayList();

        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {

            String propertyPath = constraintViolation.getPropertyPath().toString();

            String message = constraintViolation.getMessage();

            errors.add(propertyPath + ": " + message);
        }

        if (errors.size() > 0) {
            throw new PPValidateException(String.join("; ", errors));
        }
    }

    public static <T> Page<T> getPageResult(JPAQuery<T> jpaQuery, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page, size);

        jpaQuery.offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        QueryResults<T> queryResults = jpaQuery.fetchResults();

        Page<T> result = new PageImpl(queryResults.getResults(), pageable, queryResults.getTotal());

        return result;
    }

    public static PPPageInfo getPPPageInfo(Page result) {
        PPPageInfo ppPageInfo = new PPPageInfo();
        ppPageInfo.setTotalElements(result.getTotalElements());
        ppPageInfo.setTotalPages(result.getTotalPages());
        ppPageInfo.setSize(result.getSize());
        ppPageInfo.setPage(result.getNumber());
        ppPageInfo.setEmpty(result.isEmpty());

        return ppPageInfo;
    }
}

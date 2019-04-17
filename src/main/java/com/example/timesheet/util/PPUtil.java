package com.example.timesheet.util;

import com.example.timesheet.exception.PPValidateException;

import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
}

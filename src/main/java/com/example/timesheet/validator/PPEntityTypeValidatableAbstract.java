package com.example.timesheet.validator;

import com.example.timesheet.exception.PPValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@MappedSuperclass
public abstract class PPEntityTypeValidatableAbstract implements PPTypeValidatable {
    @PrePersist
    @PreUpdate
    // 配合 spring.jpa.properties.javax.persistence.validation.mode=none 使用
    public void v() {
        Set<ConstraintViolation<Object>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(this);

        List<String> errors = new ArrayList();

        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {

            String propertyPath = constraintViolation.getPropertyPath().toString();

            String message = constraintViolation.getMessage();

            errors.add(propertyPath + ": " + message);
        }

        if (errors.size() > 0) {
            throw new PPValidateException(String.join("; ", errors));
        }

        validate();
    }

    @Override
    public void validate() {

    }
}

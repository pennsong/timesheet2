package com.example.timesheet.validator;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Set;

import static com.example.timesheet.util.PPUtil.throwConstraintViolationsAsString;

@Slf4j
@MappedSuperclass
public abstract class PPEntityTypeValidatableAbstract implements PPTypeValidatable {
    @PrePersist
    @PreUpdate
    // 配合 spring.jpa.properties.javax.persistence.validation.mode=none 使用
    public void v() {
        Set<ConstraintViolation<Object>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(this);

        throwConstraintViolationsAsString(constraintViolations);

        validate();
    }

    @Override
    public void validate() {

    }
}

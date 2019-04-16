package com.example.timesheet.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Set;

import static com.example.timesheet.util.PPUtil.throwConstraintViolationsAsString;

@Slf4j
@Aspect
@Component
public class DoDtoValid {

    @Pointcut("@annotation(DtoValid)")
    public void callApi() {
    }

    @Before("callApi()")
    public void doBefore(JoinPoint joinPoint) {
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        String[] parameterNames = codeSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].equals("dto")) {
                Set<ConstraintViolation<Object>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(parameterValues[i]);

                throwConstraintViolationsAsString(constraintViolations);
            }
        }
    }
}
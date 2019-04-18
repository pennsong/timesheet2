package com.example.timesheet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class TimesheetApplication {
    public static void main(String[] args) {
        SpringApplication.run(TimesheetApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}

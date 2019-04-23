package com.example.timesheet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.h2.tools.Server;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
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
    void started()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseaServer() throws SQLException {
        return Server.createTcpServer(
                "-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        return loggingFilter;
    }
}

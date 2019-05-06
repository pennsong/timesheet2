package com.example.timesheet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Profile("dev")
@Service
public class H2Service implements DBService{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void dump(String name) {
        jdbcTemplate.execute("script to 'src/test/resources/" + name  + ".sql'");
    }

    public void restore(String name) {
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("RUNSCRIPT FROM 'src/test/resources/" + name + ".sql'");
        log.info("pptest restored");
    }
}


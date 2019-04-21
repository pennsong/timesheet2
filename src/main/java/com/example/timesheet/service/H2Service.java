package com.example.timesheet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class H2Service {
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

//    public void dump(String name) {
//        String executeCmd = "mysqldump -u " + "root" + " -p" + 123456 + " --add-drop-database -B " + "timesheet" + " -r " + "src/test/resources/" + name + ".sql";
//        Process runtimeProcess;
//        try {
//            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
//            int processComplete = runtimeProcess.waitFor();
//            if (processComplete == 0) {
//                log.info("Backup created successfully");
//            } else {
//                log.info("Could not create the backup");
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void restore(String name) {
//        String[] executeCmd = new String[]{"mysql", "--user=" + "root", "--password=" + "123456", "-e", "source " + "src/test/resources/" + name + ".sql"};
//
//        Process runtimeProcess;
//        try {
//            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
//            int processComplete = runtimeProcess.waitFor();
//            if (processComplete == 0) {
//                log.info("Backup restored successfully");
//            } else {
//                log.info("Could not restore the backup");
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}

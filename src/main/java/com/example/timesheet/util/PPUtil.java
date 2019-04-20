package com.example.timesheet.util;

import com.example.timesheet.exception.PPValidateException;
import lombok.extern.slf4j.Slf4j;

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

    public static void dump(String name) {
        String executeCmd = "mysqldump -u " + "root" + " -p" + 123456 + " --add-drop-database -B " + "timesheet" + " -r " + "src/test/resources/" + name + ".sql";
        Process runtimeProcess;
        try {
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
                log.info("Backup created successfully");
            } else {
                log.info("Could not create the backup");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void restore(String name) {
        String[] executeCmd = new String[]{"mysql", "--user=" + "root", "--password=" + "123456", "-e", "source " + "src/test/resources/" + name + ".sql"};

        Process runtimeProcess;
        try {
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
            if (processComplete == 0) {
                log.info("Backup restored successfully");
            } else {
                log.info("Could not restore the backup");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

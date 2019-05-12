package com.example.timesheet.service;

import lombok.Setter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Profile({"test", "prd"})
@Service
@ConfigurationProperties(prefix = "spring.datasource")
public class MysqlService implements DBService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Getter
    @Setter
    private String username;
    
    @Getter
    @Setter
    private String password;
    
    @Getter
    @Setter
    private String mysqldump;

    @Getter
    @Setter
    private String mysql;

    public void dump(String name) {
    	    String OS = System.getProperty("os.name").toLowerCase();
    	    String[] executeCmd = new String[] { mysqldump, "-u" + username, "-p" + password, "--add-drop-database", 
					"-B", "timesheet", "-r", "src/test/resources/" + name + ".sql" };
    	    if(OS.startsWith("win")) {
    	    		executeCmd = new String[] { "cmd", "/c", mysqldump, "-u" + username, "-p" + password, "--add-drop-database", 
    						"-B", "timesheet", "-r", "src/test/resources/" + name + ".sql" };
    	    } else if(OS.startsWith("mac os")) {
    			executeCmd = new String[] { mysqldump, "-u" + username, "-p" + password, "--add-drop-database", 
    					"-B", "timesheet", "-r", "src/test/resources/" + name + ".sql" };
        	}
        
        log.info("Exec: "+ String.join(" ", executeCmd));
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
        		log.info("Could not create the backup");
            ex.printStackTrace();
        }
    }

    public void restore(String name) {
    		String OS = System.getProperty("os.name").toLowerCase();
	    String[] executeCmd = new String[]{ mysql, "--user=" + username, "--password=" + password, "-e", "source " + "src/test/resources/" + name + ".sql"};
	    if(OS.startsWith("win")) {
	    		executeCmd = new String[] { "cmd", "/c", mysql, "--user=" + username, "--password=" + password, "-e", "source " + "src/test/resources/" + name + ".sql" };
	    } else if(OS.startsWith("mac os")) {
			executeCmd = new String[] { mysql, "--user=" + username, "--password=" + password, "-e", "source " + "src/test/resources/" + name + ".sql" };
	    }
	    log.info("Exec: "+ String.join(" ", executeCmd));
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
        		log.info("Could not create the backup");
            ex.printStackTrace();
        }
    }
}


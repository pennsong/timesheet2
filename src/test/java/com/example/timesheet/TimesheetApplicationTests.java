package com.example.timesheet;

import com.example.timesheet.repository.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class TimesheetApplicationTests {
    private static boolean init = false;

    public static final String PPOK = "1";
    public static final String PPBusinessExceptionCode = "1000";
    public static final String PPDeleteReferenceExceptionCode = "2000";
    public static final String PPDuplicateExceptionCode = "3000";
    public static final String PPReferencedExceptionCode = "3500";
    public static final String PPValidateExceptionCode = "4000";
    public static final String PPItemNotExistExceptionCode = "5000";

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    protected GongSiRepository gongSiRepository;

    @Autowired
    protected XiangMuRepository xiangMuRepository;

    @Autowired
    protected YongHuRepository yongHuRepository;

    @Autowired
    protected ZhiFuRepository zhiFuRepository;

    @Autowired
    protected GongZuoJiLuRepository gongZuoJiLuRepository;

    @Before
    public void before() throws IOException {
        if (!init) {
            init = true;
            // dump当前数据库到dump.sql
            jdbcTemplate.execute("script to 'src/test/resources/dump.sql'");
        } else {
            // drop序列生成器
            jdbcTemplate.execute("DROP SEQUENCE HIBERNATE_SEQUENCE;");

            // -DROP所有表
            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
            tables.stream().forEach(item -> jdbcTemplate.execute("DROP TABLE " + item.get("TABLE_NAME")));
            // -

            // -读取dump.sql中的sql, 依次执行
            FileReader fr = new FileReader(new File("src/test/resources/dump.sql"));
            BufferedReader br = new BufferedReader(fr);
            String lineStr;
            StringBuilder stringBuilder = new StringBuilder();
            while ((lineStr = br.readLine()) != null) {
                stringBuilder.append(lineStr);
            }
            br.close();

            String[] sqlCommands = stringBuilder.toString().split(";");

            for (String item : sqlCommands) {
                jdbcTemplate.execute(item);
            }
            log.info("pptest restored");
            // -
        }
    }

    protected String login(String yongHuMing, String miMa) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", yongHuMing);
        map.add("password", miMa);

        HttpEntity<String> response = restTemplate.postForEntity("/login",  map, String.class);
        HttpHeaders responseHeaders = response.getHeaders();
        String setCookie = responseHeaders.getFirst(responseHeaders.SET_COOKIE);

        return setCookie;
    }

    protected void checkCode(HttpEntity<String> response, String code) {
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            Assert.assertEquals(code, jsonObject.get("code"));
        } catch (Exception e) {
            Assert.assertEquals(code, e.getMessage());
        }
    }

    @Test
    public void foo() {

    }
}
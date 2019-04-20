package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.*;
import com.example.timesheet.service.H2Service;
import com.example.timesheet.service.MainService;
import com.example.timesheet.service.PPResponse;
import com.example.timesheet.util.PPJson;
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
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(isolation = Isolation.READ_COMMITTED)
public class TimesheetApplicationTests {
    public static final String PPOK = "1";
    public static final String PPBusinessExceptionCode = "1000";
    public static final String PPDeleteReferenceExceptionCode = "2000";
    public static final String PPDuplicateExceptionCode = "3000";
    public static final String PPReferencedExceptionCode = "3500";
    public static final String PPValidateExceptionCode = "4000";
    public static final String PPItemNotExistExceptionCode = "5000";

    protected static Map<String, String> cookies = new HashMap();

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected H2Service h2Service;

    @Autowired
    protected PPResponse ppResponse;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

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

    @Autowired
    protected MainService mainService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    // todo 测试下是否要控制同步synchronized
//    @Before
//    public void before() throws IOException {
//        if (!init) {
//            init = true;
//
//            // 获取登录cookies
//            String cookie = login("Admin", "1234");
//            cookies.put("Admin", cookie);
//
//            for (int i = 1; i <= 3; i++) {
//                cookie = login("y" + i, "1234");
//                cookies.put("y" + i, cookie);
//            }
//
//            // dump当前数据库到dump.sql
////            jdbcTemplate.execute("script to 'src/test/resources/dump.sql'");
//        } else {
////            // drop序列生成器
////            jdbcTemplate.execute("DROP SEQUENCE HIBERNATE_SEQUENCE;");
////
////            // -DROP所有表
////            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
////            tables.stream().forEach(item -> jdbcTemplate.execute("DROP TABLE " + item.get("TABLE_NAME")));
////            // -
////
////        // -读取dump.sql中的sql, 依次执行
////        FileReader fr = new FileReader(new File("src/test/resources/dump.sql"));
////        BufferedReader br = new BufferedReader(fr);
////        String lineStr;
////        StringBuilder stringBuilder = new StringBuilder();
////        while ((lineStr = br.readLine()) != null) {
////            if (lineStr.startsWith("--")) {
////                continue;
////            }
////            stringBuilder.append(lineStr);
////        }
////        br.close();
////
////        String[] sqlCommands = stringBuilder.toString().split(";");
////
////        for (String item : sqlCommands) {
////            jdbcTemplate.execute(item);
////        }
////        log.info("pptest restored");
////        // -
//        }
//    }

    // todo 换成restful的登录
    protected String login(String yongHuMing, String miMa) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", yongHuMing);
        map.add("password", miMa);

        HttpEntity<String> response = restTemplate.postForEntity("/login", map, String.class);
        HttpHeaders responseHeaders = response.getHeaders();
        String setCookie = responseHeaders.getFirst(responseHeaders.SET_COOKIE);

        return setCookie;
    }

    protected void checkCode(HttpEntity<String> response, String code) {
        log.info(response.getBody());
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            Assert.assertEquals(code, jsonObject.get("code"));
        } catch (Exception e) {
            Assert.assertEquals(code, e.getMessage());
        }
    }

    protected ResponseEntity<String> request(String url, HttpMethod method, String yongHuMing, String... requestBodyFieldValues) {
        PPJson ppJson = new PPJson();
        for (String item : requestBodyFieldValues) {
            String[] strArray = item.split(",");
            ppJson.put(strArray[0].trim(), strArray[1].trim());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, cookies.get(yongHuMing));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                httpHeaders
        );

        return restTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> request(String url, HttpMethod method, String yongHuMing, PPJson ppJson) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, cookies.get(yongHuMing));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                httpHeaders
        );

        return restTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> request(String url, String yongHuMing, String... requestBodyFieldValues) {
        return request(url, HttpMethod.POST, yongHuMing, requestBodyFieldValues);
    }

    @Test
    public void foo() {

    }
}
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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
public abstract class TimesheetApplicationTests {
    public static final String PPOK = "1";
    public static final String PPBusinessExceptionCode = "1000";
    public static final String PPDeleteReferenceExceptionCode = "2000";
    public static final String PPDuplicateExceptionCode = "3000";
    public static final String PPReferencedExceptionCode = "3500";
    public static final String PPValidateExceptionCode = "4000";
    public static final String PPItemNotExistExceptionCode = "5000";

    public static final int NOT_START = 0;
    public static final int INIT_DATA_DONE = 1;
    public static final int LOGIN_COOKIE_DONE = 2;

    public static boolean emptyDBDumped = false;

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

    public abstract void initData();
}
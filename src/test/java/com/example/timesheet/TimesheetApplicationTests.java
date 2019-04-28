package com.example.timesheet;

import com.example.timesheet.repository.*;
import com.example.timesheet.service.H2Service;
import com.example.timesheet.service.MainService;
import com.example.timesheet.service.PPResponse;
import com.example.timesheet.util.PPJson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.HashMap;
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

    protected static Map<String, String> jwts = new HashMap();

    @PersistenceContext
    protected EntityManager entityManager;

    @LocalServerPort
    private int port;

    @Autowired
    protected H2Service h2Service;

    @Autowired
    protected PPResponse ppResponse;

    @Autowired
    protected TestRestTemplate testRestTemplate;

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

    protected String login(String yongHuMing, String miMa) {
        PPJson ppJson = new PPJson();
        ppJson.put("username", yongHuMing);
        ppJson.put("password", miMa);

        HttpEntity<String> response = testRestTemplate.postForEntity("/login", ppJson.toString(), String.class);

        String jwt;

        try {
            JSONObject jsonObject = new JSONObject(response.getBody());

            jwt = jsonObject.getString("data");
//            log.info("pptest:" + jwt);
        } catch (Exception e) {
            jwt = "";
//            log.info("pptest: exception:" + jwt + ":" + response.getBody());
        }

        return jwt;
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
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwts.get(yongHuMing));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                httpHeaders
        );

        return testRestTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> request(String url, HttpMethod method, String yongHuMing, PPJson ppJson) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwts.get(yongHuMing));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                httpHeaders
        );

        return testRestTemplate.exchange(url, method, request, String.class);
    }

    protected ResponseEntity<String> request(String url, String yongHuMing, String... requestBodyFieldValues) {
        return request(url, HttpMethod.POST, yongHuMing, requestBodyFieldValues);
    }

    public abstract void initData();
}
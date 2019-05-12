package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.*;
import com.example.timesheet.service.DBService;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    protected DBService dbService;

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
    protected TiChengRepository tiChengRepository;
    
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

    public void basicInitData() {
        // 如没有admin则新建admin
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
        if (yongHu == null) {
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal("500"), new BigDecimal("50"), null, Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        /*
        用户
        y1 2
        y2 2
        y3 2
        */
        YongHu y1 = mainService.createYongHu("y1", "1234", new BigDecimal("2"), new BigDecimal("1"));
        YongHu y2 = mainService.createYongHu("y2", "1234", new BigDecimal("2"), new BigDecimal("1"));
        YongHu y3 = mainService.createYongHu("y3", "1234", new BigDecimal("2"), new BigDecimal("1"));

       /*
       公司
       g1
       g2
       g3
       */
        GongSi g1 = mainService.createGongSi("g1");
        GongSi g2 = mainService.createGongSi("g2");
        GongSi g3 = mainService.createGongSi("g3");

        /*
        项目
        g1x1 g1
        [
            {
                y1,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ],
                y2,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ]
            }
        ]
        g1x2 g1
        g2x1 g2
        */
        XiangMu g1x1 = mainService.createXiangMu("g1x1", g1.getId());
        XiangMu g1x2 = mainService.createXiangMu("g1x2", g1.getId());
        XiangMu g2x1 = mainService.createXiangMu("g2x1", g2.getId());

        mainService.addXiangMuChengYuan(g1x1.getId(), y1.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));
        mainService.addXiangMuTiChengBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("2"));

        mainService.addXiangMuChengYuan(g1x1.getId(), y2.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));
        mainService.addXiangMuTiChengBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("2"));

        /*
        支付
        2000/1/1 g1 100.0 testNote
        */
        mainService.createZhiFu(g1.getMingCheng(), LocalDate.of(2000, 1, 1), new BigDecimal("100"), "testNote");

        /*
        workRecord
        g1x1 y1 2000/1/1 10:01 11:01 testWorkNote
        */
        mainService.createGongZuoJiLu(
                y1.getYongHuMing(),
                g1x1.getMingCheng(),
                LocalDateTime.of(2000, 1, 1, 10, 1),
                LocalDateTime.of(2000, 1, 1, 11, 1),
                "testWorkNote"
        );
        
        /*
        提成
        2000/1/3 y1 1 testNote
        */
        mainService.createTiCheng(y1.getYongHuMing(), LocalDate.of(2000, 1, 3), new BigDecimal("1"), "testNote");
    }
}
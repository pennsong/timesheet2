package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.*;
import com.example.timesheet.service.MainService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
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
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(isolation= Isolation.READ_COMMITTED)
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
    protected EntityManagerFactory emf;

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

    @Autowired
    protected MainService mainService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

//    @Before
//    public void before() {
//        jdbcTemplate.execute("set foreign_key_checks=0");
////        // drop序列生成器
////        jdbcTemplate.execute("DROP TABLE HIBERNATE_SEQUENCE");
//
//        // -DELETE所有表
//        List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
//        tables.stream().forEach(item -> jdbcTemplate.execute("DELETE FROM " + item.get("tables_in_timesheet")));
//        // -
//
//        jdbcTemplate.execute("set foreign_key_checks=1");
//
//        // 如没有admin则新建admin
//        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
//        if (yongHu == null) {
//            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal(500), Arrays.asList("ADMIN"));
//            yongHuRepository.save(yongHu1);
//        }
//
//        /*
//        用户
//        y1 2
//        y2 2
//        y3 2
//        */
//        YongHu y1 = mainService.createYongHu("y1", "1234", new BigDecimal(2));
//        YongHu y2 = mainService.createYongHu("y2", "1234", new BigDecimal(2));
//        YongHu y3 = mainService.createYongHu("y3", "1234", new BigDecimal(2));
//
//       /*
//       公司
//       g1
//       g2
//       g3
//       */
//        GongSi g1 = mainService.createGongSi("g1");
//        GongSi g2 = mainService.createGongSi("g2");
//        GongSi g3 = mainService.createGongSi("g3");
//
//        /*
//        项目
//        g1x1 g1
//        [
//            {
//                y1,
//                xiaoShiFeiYong: [
//                    {
//                        2000/1/1,
//                        2
//                    },
//                    {
//                        2000/1/5,
//                        4
//                    },
//                    {
//                        2000/1/10,
//                        6
//                    }
//                ]
//            },
//            {
//                y2,
//                xiaoShiFeiYong: [
//                     {
//                        2000/1/1,
//                        2
//                    },
//                    {
//                        2000/1/5,
//                        4
//                    },
//                    {
//                        2000/1/10,
//                        6
//                    }
//                ]
//            }
//        ]
//        g1x2 g1
//        [
//            {
//                y1,
//                xiaoShiFeiYong: [
//                    {
//                        2000/1/1,
//                        2
//                    }
//                ]
//            }
//        ]
//        g2x1 g2
//        */
//        XiangMu g1x1 = mainService.createXiangMu("g1x1", g1.getId());
//        XiangMu g1x2 = mainService.createXiangMu("g1x2", g1.getId());
//        XiangMu g2x1 = mainService.createXiangMu("g2x1", g2.getId());
//
//        mainService.addXiangMuChengYuan(g1x1.getId(), y1.getId());
//        mainService.addXiangMuChengYuan(g1x1.getId(), y2.getId());
//
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal(2));
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 5), new BigDecimal(4));
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 10), new BigDecimal(6));
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal(2));
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 5), new BigDecimal(4));
//        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 10), new BigDecimal(6));
//
//        mainService.addXiangMuChengYuan(g1x2.getId(), y1.getId());
//
//        mainService.addXiangMuJiFeiBiaoZhun(g1x2.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal(2));
//
//        /*
//        支付
//        2000/1/1 g1 100.0 testNote
//        2000/1/5 g1 100.0 testNote
//        */
//        mainService.createZhiFu(g1.getMingCheng(), LocalDate.of(2000, 1, 1), new BigDecimal(100), "testNote");
//        mainService.createZhiFu(g1.getMingCheng(), LocalDate.of(2000, 1, 5), new BigDecimal(100), "testNote");
//
//        /*
//        workRecord
//        g1x1 y1 2000/1/1 10:01 11:01 testWorkNote
//        g1x1 y1 2000/1/5 10:01 11:01 testWorkNote
//        g1x1 y1 2000/1/6 10:01 11:01 testWorkNote
//        */
//        mainService.createGongZuoJiLu(
//                y1.getYongHuMing(),
//                g1x1.getMingCheng(),
//                LocalDateTime.of(2000, 1, 1, 10, 1),
//                LocalDateTime.of(2000, 1, 1, 11, 1),
//                "testWorkNote"
//        );
//        mainService.createGongZuoJiLu(
//                y1.getYongHuMing(),
//                g1x1.getMingCheng(),
//                LocalDateTime.of(2000, 1, 5, 10, 1),
//                LocalDateTime.of(2000, 1, 5, 11, 1),
//                "testWorkNote"
//        );
//        mainService.createGongZuoJiLu(
//                y1.getYongHuMing(),
//                g1x1.getMingCheng(),
//                LocalDateTime.of(2000, 1, 6, 10, 1),
//                LocalDateTime.of(2000, 1, 6, 11, 1),
//                "testWorkNote"
//        );
//    }

    @Before
    public void before() throws IOException {
//        if (!init) {
//            init = true;
//            // dump当前数据库到dump.sql
////            jdbcTemplate.execute("script to 'src/test/resources/dump.sql'");
//        } else {
//            // drop序列生成器
//            jdbcTemplate.execute("DROP SEQUENCE HIBERNATE_SEQUENCE;");
//
//            // -DROP所有表
//            List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
//            tables.stream().forEach(item -> jdbcTemplate.execute("DROP TABLE " + item.get("TABLE_NAME")));
//            // -

            // -读取dump.sql中的sql, 依次执行
            FileReader fr = new FileReader(new File("src/test/resources/dump.sql"));
            BufferedReader br = new BufferedReader(fr);
            String lineStr;
            StringBuilder stringBuilder = new StringBuilder();
            while ((lineStr = br.readLine()) != null) {
                if (lineStr.startsWith("--")) {
                    continue;
                }
                stringBuilder.append(lineStr);
            }
            br.close();

            String[] sqlCommands = stringBuilder.toString().split(";");

            for (String item : sqlCommands) {
                jdbcTemplate.execute(item);
            }
            log.info("pptest restored");
            // -
//        }
    }

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
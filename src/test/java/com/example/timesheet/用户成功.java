package com.example.timesheet;

import com.example.timesheet.model.*;
import com.example.timesheet.util.PPJson;
import com.example.timesheet.util.PPUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;
import org.springframework.test.annotation.Commit;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.example.timesheet.util.PPUtil.MAX_DATE;
import static com.example.timesheet.util.PPUtil.MIN_DATE;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 用户成功 extends TimesheetApplicationTests {
    private static int step = NOT_START;

    private static String dumpFileName = "yongHuChengGong";

    @Override
    public void initData() {
        // 如没有admin则新建admin
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
        if (yongHu == null) {
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal("500"), Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        /*
        用户
        y1 2
        y2 2
        y3 2
        */
        YongHu y1 = mainService.createYongHu("y1", "1234", new BigDecimal("2"));
        YongHu y2 = mainService.createYongHu("y2", "1234", new BigDecimal("2"));
        YongHu y3 = mainService.createYongHu("y3", "1234", new BigDecimal("2"));

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

        mainService.addXiangMuChengYuan(g1x1.getId(), y2.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));

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
    }

    @Before
    public void before() {
        if (step == NOT_START) {
            h2Service.restore("emptyDB");
        } else if (step == INIT_DATA_DONE) {
            h2Service.dump(dumpFileName);
        } else {
            h2Service.restore(dumpFileName);
        }
    }

    @Test
    @Commit
    public void _1initData() {
        initData();
        step = INIT_DATA_DONE;
    }

    @Test
    public void _2loginCookies() {
        // 获取登录cookies
        String cookie = login("Admin", "1234");
        cookies.put("Admin", cookie);

        for (int i = 1; i <= 3; i++) {
            cookie = login("y" + i, "1234");
            cookies.put("y" + i, cookie);
        }

        step = LOGIN_COOKIE_DONE;
    }

    // 正式测试案例开始

    @Test
    public void 设置当前用户密码() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/changePassword",
                HttpMethod.POST,
                "y1",
                "password, 5678"
        );
        checkCode(response, PPOK);


        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", "y1");
        map.add("password", "5678");

        HttpEntity<String> response1 = restTemplate.postForEntity("/login", map, String.class);
        Assert.assertEquals(true, response1.toString().contains("homepage"));
    }

    @Test
    public void 导入本人工作记录() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T00:00");
        gongZuoJiLu1.put("jieShu", "2000-01-02T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("kaiShi", "2000-01-03T00:00");
        gongZuoJiLu2.put("jieShu", "2000-01-04T00:00");
        gongZuoJiLu2.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);
        jsonArray.put(gongZuoJiLu2);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Iterable<GongZuoJiLu> gongZuoJiLus = gongZuoJiLuRepository.findAll();
        Boolean result = StreamSupport
                .stream(gongZuoJiLus.spliterator(), false)
                .anyMatch(
                        item -> item.getYongHu().getYongHuMing().equals("y1")
                                &&
                                item.getXiangMu().getMingCheng().equals("g1x1")
                                &&
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 2, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 2, 1, 1))
                                &&
                                item.getBeiZhu().equals("testNote")
                );

        Assert.assertTrue(result);

        result = StreamSupport
                .stream(gongZuoJiLus.spliterator(), false)
                .anyMatch(
                        item -> item.getYongHu().getYongHuMing().equals("y1")
                                &&
                                item.getXiangMu().getMingCheng().equals("g1x1")
                                &&
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 3, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 3, 23, 59, 59))
                                &&
                                item.getBeiZhu().equals("testNote")
                );

        Assert.assertTrue(result);

        result = StreamSupport
                .stream(gongZuoJiLus.spliterator(), false)
                .anyMatch(
                        item -> item.getYongHu().getYongHuMing().equals("y1")
                                &&
                                item.getXiangMu().getMingCheng().equals("g1x1")
                                &&
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 4, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 4, 0, 0))
                                &&
                                item.getBeiZhu().equals("testNote")
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 删除本人工作记录() {
        Optional<GongZuoJiLu> gongZuoJiLuOptional = StreamSupport.stream(gongZuoJiLuRepository.findAll().spliterator(), false)
                .filter(item -> item.getYongHu().getYongHuMing().equals("y1"))
                .findFirst();

        Long id = gongZuoJiLuOptional.get().getId();

        ResponseEntity<String> response = request(
                "/deleteGongZuoJiLu/" + id ,
                HttpMethod.DELETE,
                "y1"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongZuoJiLuOptional = gongZuoJiLuRepository.findById(id);
        Assert.assertFalse(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 查询自己的工作记录() throws JSONException {
        ResponseEntity<String> response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("g1x1", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("xiangMu_mingCheng"));
        Assert.assertEquals("y1", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("yongHu_yongHuMing"));
        Assert.assertEquals("2000-01-01T10:01:00", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("kaiShi"));
        Assert.assertEquals("2000-01-01T11:01:00", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("jieShu"));
        Assert.assertEquals("testWorkNote", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("beiZhu"));
    }
}

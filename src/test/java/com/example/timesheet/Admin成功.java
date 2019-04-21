package com.example.timesheet;

import com.example.timesheet.model.*;
import com.example.timesheet.util.PPJson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
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

import static com.example.timesheet.util.PPUtil.MIN_DATE;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Admin成功 extends TimesheetApplicationTests {
    private static int step = NOT_START;

    private static String dumpFileName = "adminChengGong";

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
    public void 新建用户() {
        ResponseEntity<String> response = request(
                "/admin/createYongHu",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, yt1",
                "miMa, 1234",
                "xiaoShiFeiYong, 500"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        YongHu yongHu = yongHuRepository.findOneByYongHuMing("yt1");
        Assert.assertNotNull(yongHu);
    }

    @Test
    public void 删除用户() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        ResponseEntity<String> response = request(
                "/admin/deleteYongHu/" + yongHu.getId(),
                HttpMethod.DELETE,
                "Admin",
                "mingCheng, gt1"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        yongHu = yongHuRepository.findOneByYongHuMing("y3");
        Assert.assertNull(yongHu);
    }

    @Test
    public void 设置指定用户密码() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/setYongHuPassword",
                HttpMethod.POST,
                "Admin",
                "yongHuId," + yongHu.getId(),
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
    public void 新建公司() {
        ResponseEntity<String> response = request(
                "/admin/createGongSi",
                HttpMethod.POST,
                "Admin",
                "mingCheng, gt1"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        GongSi gongSi = gongSiRepository.findOneByMingCheng("gt1");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 删除公司() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g3");

        ResponseEntity<String> response = request(
                "/admin/deleteGongSi/" + +gongSi.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g3");
        Assert.assertNull(gongSi);
    }

    @Test
    public void 设置公司名称() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/setGongSiMingCheng/",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "mingCheng, g1c"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1c");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 设置公司结算日() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "jieSuanRi, 2000-01-01"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1");
        Assert.assertEquals(true, gongSi.getJieSuanRi().isEqual(LocalDate.of(2000, 1, 1)));
    }

    @Test
    public void 新建项目() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "mingCheng, g1x3",
                "gongSiId, " + gongSi.getId()
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x3");
        Assert.assertNotNull(xiangMu);
        Assert.assertEquals(gongSi.getId(), xiangMu.getGongSi().getId());
    }

    @Test
    public void 删除项目() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x2");

        ResponseEntity<String> response = request(
                "/admin/deleteXiangMu/" + xiangMu.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x2");
        Assert.assertNull(xiangMu);
    }

    @Test
    public void 添加项目计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId(),
                "kaiShi, 2000-02-01",
                "xiaoShiFeiYong, 501"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .anyMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 2, 1))
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(new BigDecimal("501")) == 0
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 移除项目计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/removeXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId(),
                "kaiShi, 2000-01-01"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .noneMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 1, 1))
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 添加项目成员() {
        log.info("pptest: 添加项目成员1");
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        ResponseEntity<String> response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        long count = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .filter(
                        item ->
                                item.getKaiShi().isEqual(MIN_DATE)
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(yongHu.getXiaoShiFeiYong()) == 0
                ).count();

        log.info("pptest: 添加项目成员2");
        Assert.assertTrue(count == 1);
    }

    @Test
    public void 移除项目成员() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y2");

        ResponseEntity<String> response = request(
                "/admin/removeXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .noneMatch(
                        item ->
                                item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 导入用户工作记录() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T00:00");
        gongZuoJiLu1.put("jieShu", "2000-01-02T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("yongHuMing", "y1");
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
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
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
    public void 删除工作记录() {
        Optional<GongZuoJiLu> gongZuoJiLuOptional = StreamSupport.stream(gongZuoJiLuRepository.findAll().spliterator(), false).findFirst();

        Long id = gongZuoJiLuOptional.get().getId();

        ResponseEntity<String> response = request(
                "/admin/deleteYongHuGongZuoJiLu/" + id,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongZuoJiLuOptional = gongZuoJiLuRepository.findById(id);
        Assert.assertFalse(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 新建支付() {
        ResponseEntity<String> response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, g1",
                "riQi, 2000-03-01",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Boolean result = StreamSupport.stream(zhiFuRepository.findAll().spliterator(), false)
                .anyMatch(item -> item.getGongSi().getMingCheng().equals("g1")
                        && item.getRiQi().isEqual(LocalDate.of(2000, 3, 1))
                        && item.getJingE().compareTo(new BigDecimal("1.1")) == 0
                        && item.getBeiZhu().equals("testPayment")
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 删除支付() {
        Optional<ZhiFu> zhiFuOptional = StreamSupport.stream(zhiFuRepository.findAll().spliterator(), false).findFirst();

        Long id = zhiFuOptional.get().getId();

        ResponseEntity<String> response = request(
                "/admin/deleteZhiFu/" + id,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        zhiFuOptional = zhiFuRepository.findById(id);
        Assert.assertFalse(zhiFuOptional.isPresent());
    }

    @Test
    public void 生成报告() throws JSONException {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gongSi.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-31"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(96, ((JSONObject) (jsonObject.get("data"))).get("期末Balance"));

        // 检查成功设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi.getJieSuanRi().isEqual(LocalDate.of(2900, 12, 31));

    }
}

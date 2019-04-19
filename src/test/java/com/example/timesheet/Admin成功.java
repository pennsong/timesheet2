package com.example.timesheet;

import com.example.timesheet.model.*;
import com.example.timesheet.util.PPJson;
import com.mysql.cj.xdevapi.JsonArray;
import jdk.nashorn.internal.scripts.JS;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.example.timesheet.util.PPUtil.MAX_DATE;
import static com.example.timesheet.util.PPUtil.MIN_DATE;

@Slf4j
public class Admin成功 extends TimesheetApplicationTests {
    private static HttpHeaders headers;

    @Before
    public void login() {
        if (headers == null) {
            headers = new HttpHeaders();
            String setCookie = super.login("Admin", "1234");
            headers.add(HttpHeaders.COOKIE, setCookie);
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
    }

    @Test
    public void 新建用户() {
        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "gt1");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createGongSi", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        GongSi gongSi = gongSiRepository.findOneByMingCheng("gt1");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 删除用户() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteYongHu/" + yongHu.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        yongHu = yongHuRepository.findOneByYongHuMing("y3");
        Assert.assertNull(yongHu);
    }

    @Test
    public void 设置指定用户密码() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        PPJson ppJson = new PPJson();
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("password", "5678");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/setYongHuPassword", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", "y1");
        map.add("password", "5678");

        HttpEntity<String> response1 = restTemplate.postForEntity("/login", map, String.class);
        Assert.assertEquals(true, response1.toString().contains("homepage"));
    }

    @Test
    public void 新建公司() {
        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "gt1");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createGongSi", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        GongSi gongSi = gongSiRepository.findOneByMingCheng("gt1");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 删除公司() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g3");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteGongSi/" + gongSi.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g3");
        Assert.assertNull(gongSi);
    }

    @Test
    public void 设置公司名称() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("id", gongSi.getId());
        ppJson.put("mingCheng", "g1c");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/setGongSiMingCheng", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1c");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 设置公司结算日() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("id", gongSi.getId());
        ppJson.put("jieSuanRi", "2000-01-01");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/setGongSiJieSuanRi", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1");
        Assert.assertEquals(true, gongSi.getJieSuanRi().isEqual(LocalDate.of(2000, 1, 1)));
    }

    @Test
    public void 新建项目() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "g1x3");
        ppJson.put("gongSiId", gongSi.getId());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createXiangMu", HttpMethod.POST, request, String.class);
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

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteXiangMu/" + xiangMu.getId(), HttpMethod.DELETE, request, String.class);
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

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-02-01");
        ppJson.put("xiaoShiFeiYong", "501");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
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
                                        item.getYongHu().getId() == yongHu.getId()
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 移除项目计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-01-01");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/removeXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
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
                                        item.getYongHu().getId() == yongHu.getId()
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 添加项目成员() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/addXiangMuChengYuan", HttpMethod.POST, request, String.class);
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
                                        item.getYongHu().getId() == yongHu.getId()
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(yongHu.getXiaoShiFeiYong()) == 0
                ).count();

        Assert.assertTrue(count == 1);
    }

    @Test
    public void 移除项目成员() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y2");

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/removeXiangMuChengYuan", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .noneMatch(
                        item ->
                                item.getYongHu().getId() == yongHu.getId()
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
        gongZuoJiLu2.put("kaiShi", "2000-01-02T00:00");
        gongZuoJiLu2.put("jieShu", "2000-01-03T00:00");
        gongZuoJiLu2.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);
        jsonArray.put(gongZuoJiLu2);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        log.info(ppJson.toString());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/importYongHuGongZuoJiLu", HttpMethod.POST, request, String.class);
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
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 2, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 2, 23, 59, 59))
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
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 3, 0, 0))
                                &&
                                item.getBeiZhu().equals("testNote")
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 删除工作记录() {
        Optional<GongZuoJiLu> gongZuoJiLuOptional = StreamSupport.stream(gongZuoJiLuRepository.findAll().spliterator(), false).findFirst();

        Long id = gongZuoJiLuOptional.get().getId();

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteYongHuGongZuoJiLu/" + id, HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongZuoJiLuOptional = gongZuoJiLuRepository.findById(id);
        Assert.assertFalse(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 新建支付() {
        PPJson ppJson = new PPJson();
        ppJson.put("gongSiMingCheng", "g1");
        ppJson.put("riQi", "2000-03-01");
        ppJson.put("jinE", "1.1");
        ppJson.put("beiZhu", "testPayment");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createZhiFu/", HttpMethod.POST, request, String.class);
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

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteZhiFu/" + id, HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        zhiFuOptional = zhiFuRepository.findById(id);
        Assert.assertFalse(zhiFuOptional.isPresent());
    }

    @Test
    public void 生成报告() throws JSONException {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("gongSiId", gongSi.getId());
        ppJson.put("kaiShi", MIN_DATE);
        ppJson.put("jieShu", MAX_DATE);

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/generateBaoGao", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(96, ((JSONObject) (jsonObject.get("data"))).get("期末Balance:"));
    }
}

package com.example.timesheet;

import com.example.timesheet.model.*;
import com.example.timesheet.util.PPJson;
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
public class 用户成功 extends TimesheetApplicationTests {
    private static HttpHeaders headers;

    @Before
    public void login() {
        if (headers == null) {
            headers = new HttpHeaders();
            String setCookie = super.login("y1", "1234");
            headers.add(HttpHeaders.COOKIE, setCookie);
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
    }

    @Test
    public void 设置当前用户密码() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        PPJson ppJson = new PPJson();
        ppJson.put("password", "5678");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/changePassword", HttpMethod.POST, request, String.class);
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

        ResponseEntity<String> response = restTemplate.exchange("/importGongZuoJiLu", HttpMethod.POST, request, String.class);
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
    public void 删除本人工作记录() {
        Optional<GongZuoJiLu> gongZuoJiLuOptional = StreamSupport.stream(gongZuoJiLuRepository.findAll().spliterator(), false)
                .filter(item -> item.getYongHu().getYongHuMing().equals("y1"))
                .findFirst();

        Long id = gongZuoJiLuOptional.get().getId();

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/deleteGongZuoJiLu/" + id, HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongZuoJiLuOptional = gongZuoJiLuRepository.findById(id);
        Assert.assertFalse(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 查询自己的工作记录() throws JSONException {
        PPJson ppJson = new PPJson();

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/queryGongZuoJiLu", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("g1x1", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("xiangMu_mingCheng"));
        Assert.assertEquals("y1", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("yongHu_yongHuMing"));
        Assert.assertEquals("2000-01-01T10:01:00", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("kaiShi"));
        Assert.assertEquals("2000-01-01T11:01:00", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("jieShu"));
        Assert.assertEquals("testWorkNote", new JSONObject(response.getBody()).getJSONArray("data").getJSONObject(0).get("beiZhu"));
    }
}

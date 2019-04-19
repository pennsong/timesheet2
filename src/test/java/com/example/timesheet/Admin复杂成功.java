package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Admin复杂成功 extends TimesheetApplicationTests {
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
    public void 删除项目_$新建项目_添加成员_添加计费标准_删除项目() {
        // 新建项目
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "g1xt1");
        ppJson.put("gongSiId", gongSi.getId());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createXiangMu", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 添加成员
        Long xiangMuId = ppResponse.gainId(response);
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMuId);
        ppJson.put("yongHuId", yongHu.getId());

        request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        response = restTemplate.exchange("/admin/addXiangMuChengYuan", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 添加计费标准
        ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMuId);
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-01-01");
        ppJson.put("xiaoShiFeiYong", "1.1");

        request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
        checkCode(response, PPOK);

        // 删除项目
        request = new HttpEntity<>(
                headers
        );

        response = restTemplate.exchange("/admin/deleteXiangMu/" + xiangMuId, HttpMethod.DELETE, request, String.class);
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1xt1");
        Assert.assertNull(gongSi);
    }

    @Test
    public void 添加项目计费标准_$添加计费标准_添加同一天的计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 添加计费标准
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

        // 添加同一天的计费标准
        ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-02-01");
        ppJson.put("xiaoShiFeiYong", "502");

        request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
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

        Assert.assertFalse(result);

        result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .anyMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 2, 1))
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(new BigDecimal("502")) == 0
                                        &&
                                        item.getYongHu().getId() == yongHu.getId()
                );

        Assert.assertTrue(result);
    }

    // todo 复杂报告
}

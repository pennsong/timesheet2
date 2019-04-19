package com.example.timesheet;

import com.example.timesheet.controller.MainController;
import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import com.example.timesheet.util.PPUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Admin extends TimesheetApplicationTests {

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
    public void 新建用户_失败_重名() {
        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "g1");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createGongSi", HttpMethod.POST, request, String.class);
        checkCode(response, PPDuplicateExceptionCode);
    }



    @Test
    public void 删除用户_失败_有分配到项目() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y2");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteYongHu/" + yongHu.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPReferencedExceptionCode);

        yongHu = yongHuRepository.findOneByYongHuMing("y2");
        Assert.assertNotNull(yongHu);
    }

    @Test
    public void 删除用户_失败_有工作记录() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteYongHu/" + yongHu.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPReferencedExceptionCode);

        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        Assert.assertNotNull(yongHu);
    }





    @Test
    public void 新建公司_失败_重名() {
        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "g1");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createGongSi", HttpMethod.POST, request, String.class);
        checkCode(response, PPDuplicateExceptionCode);
    }



    @Test
    public void 删除公司_失败_有项目() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteGongSi/" + gongSi.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPReferencedExceptionCode);

        gongSi = gongSiRepository.findOneByMingCheng("g1");
        Assert.assertNotNull(gongSi);
    }



    @Test
    public void 设置公司名称_失败_重名() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("id", gongSi.getId());
        ppJson.put("mingCheng", "g2");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/setGongSiMingCheng", HttpMethod.POST, request, String.class);
        checkCode(response, PPDuplicateExceptionCode);
    }



    @Test
    public void 设置公司结算日_失败_日期不合法() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("id", gongSi.getId());
        ppJson.put("jieSuanRi", "2000-02-31");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/setGongSiJieSuanRi", HttpMethod.POST, request, String.class);
        checkCode(response, PPValidateExceptionCode);
    }


    @Test
    public void 新建项目_失败_重名() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        PPJson ppJson = new PPJson();
        ppJson.put("mingCheng", "g1x1");
        ppJson.put("gongSiId", gongSi.getId());

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/createXiangMu", HttpMethod.POST, request, String.class);
        checkCode(response, PPDuplicateExceptionCode);
    }



    @Test
    public void 删除项目_失败_有工作记录() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");

        HttpEntity<String> request = new HttpEntity<>(
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/deleteXiangMu/" + xiangMu.getId(), HttpMethod.DELETE, request, String.class);
        checkCode(response, PPReferencedExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Assert.assertNotNull(xiangMu);
    }

    @Test
    public void 添加项目计费标准_成功_公司还未有结算日_修改已有计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        int count = xiangMu.getJiFeiBiaoZhuns().size();

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-01-10");
        ppJson.put("xiaoShiFeiYong", "501");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
        checkCode(response, PPReferencedExceptionCode);

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
                );

        int newCount = xiangMu.getJiFeiBiaoZhuns().size();

        Assert.assertTrue(result);
        Assert.assertEquals(count, newCount);
    }

    @Test
    public void 添加项目计费标准_成功_在公司结算日之后() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        int count = xiangMu.getJiFeiBiaoZhuns().size();

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
                        item -> item.getYongHu().getId() == yongHu.getId()
                                &&
                                item.getKaiShi().isEqual(LocalDate.of(2000, 2, 1))
                                &&
                                item.getXiaoShiFeiYong().compareTo(new BigDecimal("501")) == 0
                );

        int newCount = xiangMu.getJiFeiBiaoZhuns().size();

        Assert.assertTrue(result);
        Assert.assertEquals(count + 1, newCount);
    }

    @Test
    public void 添加项目计费标准_失败_在所属公司结算日() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        int count = xiangMu.getJiFeiBiaoZhuns().size();

        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-01-31");
        ppJson.put("xiaoShiFeiYong", "501");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");

        int newCount = xiangMu.getJiFeiBiaoZhuns().size();
        Assert.assertEquals(count, newCount);
    }

    @Test
    public void 添加项目计费标准_失败_在所属公司结算日之前() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        int count = xiangMu.getJiFeiBiaoZhuns().size();

        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        PPJson ppJson = new PPJson();
        ppJson.put("xiangMuId", xiangMu.getId());
        ppJson.put("yongHuId", yongHu.getId());
        ppJson.put("kaiShi", "2000-01-30");
        ppJson.put("xiaoShiFeiYong", "501");

        HttpEntity<String> request = new HttpEntity<>(
                ppJson.toString(),
                headers
        );

        ResponseEntity<String> response = restTemplate.exchange("/admin/addXiangMuJiFeiBiaoZhun", HttpMethod.POST, request, String.class);
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");

        int newCount = xiangMu.getJiFeiBiaoZhuns().size();
        Assert.assertEquals(count, newCount);
    }
}

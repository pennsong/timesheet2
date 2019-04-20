package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import com.example.timesheet.util.PPUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Admin复杂成功 extends TimesheetApplicationTests {
    private static boolean init = false;

    @Before
    public void before() {
        if (!init) {
            init = true;

            h2Service.restore("emptyDB");

            ResponseEntity<String> response = request(
                    "/test/adminFuZaChengGong",
                    HttpMethod.GET,
                    null
            );
            checkCode(response, PPOK);

            h2Service.dump("adminFuZaChengGong");

            // 获取登录cookies
            String cookie = login("Admin", "1234");
            cookies.put("Admin", cookie);

            for (int i = 1; i <= 3; i++) {
                cookie = login("y" + i, "1234");
                cookies.put("y" + i, cookie);
            }
        } else {
            h2Service.restore("adminFuZaChengGong");
        }
    }

    @Test
    public void 删除项目_$新建项目_添加成员_添加计费标准_删除项目() {
        // 新建项目
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "mingCheng, g1xt1",
                "gongSiId, " + gongSi.getId()
        );
        checkCode(response, PPOK);

        // 添加成员
        Long xiangMuId = ppResponse.gainId(response);
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMuId,
                "yongHuId, " + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 添加计费标准
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMuId,
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "xiaoShiFeiYong, 1.1"
        );
        checkCode(response, PPOK);

        // 删除项目
        response = request(
                "/admin/deleteXiangMu/" + xiangMuId,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1xt1");
        Assert.assertNull(gongSi);
    }



    @Test
    public void 添加项目计费标准_$设置公司结算日_添加结算日后的计费标准_添加同一天的计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 设置公司结算日
        ResponseEntity<String>  response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 添加结算日后的计费标准
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-01",
                "xiaoShiFeiYong, 501"
        );
        checkCode(response, PPOK);

        // 添加同一天的计费标准
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-01",
                "xiaoShiFeiYong, 502"
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

        Assert.assertFalse(result);

        result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .anyMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 2, 1))
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(new BigDecimal("502")) == 0
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertTrue(result);
    }

    @Test
    public void 生成报告_多次生成报告后结算日被设置成最晚一次的结束时间() throws JSONException {
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

        response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gongSi.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-30"
        );
        checkCode(response, PPOK);

        // 检查成功设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi.getJieSuanRi().isEqual(LocalDate.of(2900, 12, 31));

    }

// todo 复杂报告
}

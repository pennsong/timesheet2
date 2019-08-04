package com.example.timesheet.admin.generateYongHuBaoGao;

import com.example.timesheet.TimesheetApplicationTests;
import com.example.timesheet.model.*;
import com.example.timesheet.util.PPJson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 简单成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.generateYongHuBaoGao.JianDanChengGong";

    @Override
    public void initData() {
        basicInitData();
    }
    
    @BeforeTransaction
    void bt() {
        if (!init) {
            init = true;
            dbService.restore("emptyDB");
            initData();
            dbService.dump(dumpFileName);

            // 获取登录cookies
            String cookie = login("Admin", "1234");
            jwts.put("Admin", cookie);

            for (int i = 1; i <= 3; i++) {
                cookie = login("y" + i, "1234");
                jwts.put("y" + i, cookie);
            }
        } else {
            dbService.restore(dumpFileName);
        }
    }

    // 正式测试案例开始
    @Test
    public void 生成用户报告_设结算日() throws JSONException {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/generateYongHuBaoGao",
                HttpMethod.POST,
                "Admin",
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-31",
                "setJieSuanRi, true"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray xmhz = ((JSONObject)jsonObject.get("data")).getJSONArray("项目汇总");
        Assert.assertEquals(1, xmhz.length());
        Assert.assertEquals("g1x1", xmhz.getJSONObject(0).getString("项目"));
//        Assert.assertEquals("gt1x2", xmhz.getJSONObject(1).getString("项目"));
        Assert.assertEquals(1, xmhz.getJSONObject(0).getInt("耗时"));
//        Assert.assertEquals(2, xmhz.getJSONObject(1).getInt("耗时"));
//        Assert.assertTrue( Math.abs(1-((JSONObject) (jsonObject.get("data"))).getDouble("期末Balance")) < 0.0000001);
//        Assert.assertEquals(0, ((JSONObject) (jsonObject.get("data"))).get("期末Balance"));

        // 检查成功设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();
        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        yongHu.getJieSuanRi().isEqual(LocalDate.of(2900, 12, 31));

    }
    
    @Test
    public void 生成用户报告_不设结算日() throws JSONException {
    		YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/generateYongHuBaoGao",
                HttpMethod.POST,
                "Admin",
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-31",
                "setJieSuanRi, false"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray xmhz = ((JSONObject)jsonObject.get("data")).getJSONArray("项目汇总");
        Assert.assertEquals(1, xmhz.length());
        Assert.assertEquals("g1x1", xmhz.getJSONObject(0).getString("项目"));
//        Assert.assertEquals("gt1x2", xmhz.getJSONObject(1).getString("项目"));
        Assert.assertEquals(1, xmhz.getJSONObject(0).getInt("耗时"));
//        Assert.assertEquals(2, xmhz.getJSONObject(1).getInt("耗时"));
        Assert.assertTrue( Math.abs(1-((JSONObject) (jsonObject.get("data"))).getDouble("期末Balance")) < 0.0000001);

        // 检查应当未更新设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();
        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        yongHu.getJieSuanRi().isEqual(LocalDate.of(1900, 1, 1));

    }
    
    @Test
    public void 生成用户报告_开始时间等于结束时间() throws JSONException {
    		YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/generateYongHuBaoGao",
                HttpMethod.POST,
                "Admin",
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "jieShu, 2000-01-01",
                "setJieSuanRi, true"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray xmhz = ((JSONObject)jsonObject.get("data")).getJSONArray("项目汇总");
        Assert.assertEquals(1, xmhz.length());
        Assert.assertEquals("g1x1", xmhz.getJSONObject(0).getString("项目"));
//        Assert.assertEquals("gt1x2", xmhz.getJSONObject(1).getString("项目"));
        Assert.assertEquals(1, xmhz.getJSONObject(0).getInt("耗时"));
//        Assert.assertEquals(2, xmhz.getJSONObject(1).getInt("耗时"));
        Assert.assertTrue( Math.abs(2-((JSONObject) (jsonObject.get("data"))).getDouble("期末Balance")) < 0.0000001);

        // 检查成功设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();
        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        yongHu.getJieSuanRi().isEqual(LocalDate.of(2000, 1, 1));
    }
}

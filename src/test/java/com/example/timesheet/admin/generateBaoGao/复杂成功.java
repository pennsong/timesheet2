package com.example.timesheet.admin.generateBaoGao;

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
public class 复杂成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.generateBaoGao.FuZaChengGong";

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
    public void 生成报告_多次生成报告后结算日被设置成最晚一次的结束时间() throws JSONException {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gongSi.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-31",
                "setJieSuanRi, true"
        );
        checkCode(response, PPOK);

        response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gongSi.getId(),
                "kaiShi, 1900-01-01",
                "jieShu, 2900-12-30",
                "setJieSuanRi, true"
        );
        checkCode(response, PPOK);

        // 检查成功设置结算日
        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();
        gongSi = gongSiRepository.findOneByMingCheng("g1");
        gongSi.getJieSuanRi().isEqual(LocalDate.of(2900, 12, 31));

    }

    /**
     * 生成报告_复杂操作后的报告
     * <p>
     * 新建用户yt1 500
     * <p>
     * 新建用户yt2 500
     * <p>
     * 新建公司gt1
     * <p>
     * 新建项目gt1x1
     * <p>
     * 新建项目gt1x2
     * <p>
     * 添加成员yt1 gt1x1
     * <p>
     * 添加成员yt2 gt1x1
     * <p>
     * 添加成员yt1 gt1x2
     * <p>
     * 添加成员yt2 gt1x2
     * <p>
     * 导入工作记录:<br>
     * yt1 gt1x1 2000-01-01T10:00 2000-01-02T10:00 testNote1
     * yt2 gt1x1 2000-01-01T10:00 2000-01-02T10:00 testNote2
     * yt1 gt1x2 2000-01-02T10:01 2000-01-02T11:01 testNote3
     * yt2 gt1x2 2000-01-02T10:01 2000-01-02T11:01 testNote4
     * <p>
     * 添加支付:<br>
     * gt1 2000-01-03 2000.01
     * gt1 2000-01-03 999.99
     * <p>
     * 添加计费标准:<br>
     * gt1x1 yt1 2000-01-10 2000
     * gt1x1 yt1 2000-01-04 1000
     * <p>
     * 导入工作记录:<br>
     * yt1 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote5
     * yt2 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote6
     * <p>
     * 添加计费标准:<br>
     * gt1x1 yt2 2000-01-04 1000
     * <p>
     * 添加支付:<br>
     * gt1 2000-01-05 2000
     * <p>
     * 生成gt1 2000-01-02 到 2000-01-04的报告
     * <p>
     * 确认<br>
     * 期初balance: -14000
     * 期末balance: -50000
     */
    @Test
    public void 生成报告_复杂操作后的报告() throws JSONException {
        // 新建用户yt1 500
        ResponseEntity<String> response = request(
                "/admin/createYongHu",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, yt1",
                "kaiShi, 1900-01-01",
                "miMa, 1234",
                "xiaoShiFeiYong, 500",
                "xiaoShiTiCheng, 50"
        );
        checkCode(response, PPOK);
        Long yt1Id = ppResponse.gainId(response);

        // 新建用户yt2 500
        response = request(
                "/admin/createYongHu",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, yt2",
                "kaiShi, 1900-01-01",
                "miMa, 1234",
                "xiaoShiFeiYong, 500",
                "xiaoShiTiCheng, 50"
        );
        checkCode(response, PPOK);
        Long yt2Id = ppResponse.gainId(response);

        // 新建公司gt1
        response = request(
                "/admin/createGongSi",
                HttpMethod.POST,
                "Admin",
                "mingCheng, gt1"
        );
        checkCode(response, PPOK);
        Long gt1Id = ppResponse.gainId(response);

        // 新建项目gt1x1
        response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "mingCheng, gt1x1",
                "gongSiId," + gt1Id
        );
        checkCode(response, PPOK);
        Long gt1x1Id = ppResponse.gainId(response);

        // 新建项目gt1x2
        response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "mingCheng, gt1x2",
                "gongSiId," + gt1Id
        );
        checkCode(response, PPOK);
        Long gt1x2Id = ppResponse.gainId(response);

        // 添加成员yt1 gt1x1
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yt1Id
        );
        checkCode(response, PPOK);

        // 添加成员yt2 gt1x1
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yt2Id
        );
        checkCode(response, PPOK);

        // 添加成员yt1 gt1x2
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x2Id,
                "yongHuId," + yt1Id
        );
        checkCode(response, PPOK);

        // 添加成员yt2 gt1x2
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x2Id,
                "yongHuId," + yt2Id
        );
        checkCode(response, PPOK);

        // 导入工作记录
        JSONArray jsonArray = new JSONArray();

        // yt1 gt1x1 2000-01-01T10:00 2000-01-02T10:00 testNote1
        PPJson gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-01T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-02T10:00");
        gongZuoJiLu.put("beiZhu", "testNote1");
        jsonArray.put(gongZuoJiLu);

        // yt2 gt1x1 2000-01-01T10:00 2000-01-02T10:00 testNote2
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt2");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-01T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-02T10:00");
        gongZuoJiLu.put("beiZhu", "testNote2");
        jsonArray.put(gongZuoJiLu);

        // yt1 gt1x2 2000-01-02T10:01 2000-01-02T11:01 testNote3
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x2");
        gongZuoJiLu.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu.put("beiZhu", "testNote3");
        jsonArray.put(gongZuoJiLu);

        // yt2 gt1x2 2000-01-02T10:01 2000-01-02T11:01 testNote4
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt2");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x2");
        gongZuoJiLu.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu.put("beiZhu", "testNote4");
        jsonArray.put(gongZuoJiLu);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);

        // 添加支付
        // gt1 2000-01-03 2000.01
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, gt1",
                "riQi, 2000-01-03",
                "jinE, 2000.01",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // gt1 2000-01-03 999.99
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, gt1",
                "riQi, 2000-01-03",
                "jinE, 999.99",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // 添加计费标准
        // gt1x1 yt1 2000-01-10 2000
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yt1Id,
                "kaiShi, 2000-01-10",
                "xiaoShiFeiYong, 2000"
        );
        checkCode(response, PPOK);

        // gt1x1 yt1 2000-01-04 1000
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yt1Id,
                "kaiShi, 2000-01-04",
                "xiaoShiFeiYong, 1000"
        );
        checkCode(response, PPOK);

        // 导入工作记录
        jsonArray = new JSONArray();

        // yt1 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote5
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-04T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-05T10:00");
        gongZuoJiLu.put("beiZhu", "testNote5");
        jsonArray.put(gongZuoJiLu);

        // yt2 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote6
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "yt2");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-04T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-05T10:00");
        gongZuoJiLu.put("beiZhu", "testNote6");
        jsonArray.put(gongZuoJiLu);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);

        // 添加计费标准
        // gt1x1 yt2 2000-01-04 1000
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yt2Id,
                "kaiShi, 2000-01-04",
                "xiaoShiFeiYong, 1000"
        );
        checkCode(response, PPOK);

        // 添加支付
        // gt1 2000-01-05 2000
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, gt1",
                "riQi, 2000-01-05",
                "jinE, 2000",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // 生成gt1 2000-01-02 到 2000-01-04的报告
        response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gt1Id,
                "kaiShi, 2000-01-02",
                "jieShu, 2000-01-04",
                "setJieSuanRi, true"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        double qiChuBalance = jsonObject.getJSONObject("data").getDouble("期初Balance");
        double qiMoBalance = jsonObject.getJSONObject("data").getDouble("期末Balance");

        Assert.assertTrue(Math.abs((-14000) - qiChuBalance) < 1);
        Assert.assertTrue(Math.abs((-50000) - qiMoBalance) < 1);

        JSONArray xmhz = ((JSONObject)jsonObject.get("data")).getJSONArray("项目汇总");
//        System.out.println(xmhz);
        Assert.assertEquals(2, xmhz.length());
        Assert.assertEquals("gt1x1", xmhz.getJSONObject(0).getString("项目"));
        Assert.assertEquals("gt1x2", xmhz.getJSONObject(1).getString("项目"));
        Assert.assertEquals(48, xmhz.getJSONObject(0).getInt("耗时"));
        Assert.assertEquals(2, xmhz.getJSONObject(1).getInt("耗时"));
    }
    
}

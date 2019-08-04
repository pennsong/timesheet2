package com.example.timesheet.user.generateOwnYongHuBaoGao;

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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 复杂成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "user.generateOwnYongHuBaoGao.FuZaChengGong";

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
    /**
     * 生成本人用户报告_复杂操作后的报告
     * <p>
     * 新建公司gt1
     * <p>
     * 新建项目gt1x1
     * <p>
     * 新建项目gt1x2
     * <p>
     * 添加成员y1 gt1x1
     * <p>
     * 添加成员y1 gt1x2
     * <p>
     * 添加提成标准:<br>
     * gt1x1 y1 1990-01-01 50
     * gt1x2 y1 1990-01-01 50
     * <p>
     * 导入工作记录:<br>
     * y1 gt1x1 2000-01-02T10:00 2000-01-03T10:00 testNote1
     * y1 gt1x2 2000-01-03T10:00 2000-01-03T11:00 testNote2
     * <p>
     * 设置用户个人结算日 y1 2000-01-03
     * <p>
     * 添加提成:<br>
     * y1 2000-01-04 20
     * <p>
     * 添加提成标准:<br>
     * gt1x1 y1 2000-01-10 200
     * gt1x1 y1 2000-01-04 100
     * <p>
     * 导入工作记录:<br>
     * y1 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote3
     * <p>
     * 设置用户个人结算日 y1 2000-01-04
     * <p>
     * 添加提成:<br>
     * y1 2000-01-06 20
     * <p>
     * 生成y1 2000-01-02 到 2000-01-05的报告
     * <p>
     * 确认<br>
     * 期初balance: 2
     * 期末balance: 3631
     */
    @Test
    public void 生成本人用户报告_复杂操作后的报告() throws JSONException {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 新建公司gt1
        ResponseEntity<String> response = request(
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

        // 添加成员y1 gt1x1
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 添加成员y1 gt1x2
        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x2Id,
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 添加提成标准
        // gt1x1 y1 1990-01-01 50
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yongHu.getId(),
                "kaiShi, 1990-01-01",
                "xiaoShiTiCheng, 50"
        );
        checkCode(response, PPOK);
        
        // gt1x2 y1 1990-01-01 50
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x2Id,
                "yongHuId," + yongHu.getId(),
                "kaiShi, 1990-01-01",
                "xiaoShiTiCheng, 50"
        );
        checkCode(response, PPOK);
        
        // 导入工作记录
        JSONArray jsonArray = new JSONArray();

        // y1 gt1x1 2000-01-01T10:00 2000-01-02T10:00 testNote1
        PPJson gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "y1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-03T10:00");
        gongZuoJiLu.put("beiZhu", "testNote1");
        jsonArray.put(gongZuoJiLu);

        // y1 gt1x2 2000-01-02T10:00 2000-01-02T11:00 testNote2
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "y1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x2");
        gongZuoJiLu.put("kaiShi", "2000-01-03T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-03T11:00");
        gongZuoJiLu.put("beiZhu", "testNote2");
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
        
        // 设置用户个人结算日 y1 2000-01-03
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-03"
        );
        checkCode(response, PPOK);
        
        // 添加提成
        // y1 2000-01-04 20
        response = request(
                "/admin/createTiCheng",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, y1",
                "riQi, 2000-01-04",
                "jinE, 20",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // 添加提成标准
        // gt1x1 y1 2000-01-10 200
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yongHu.getId(),
                "kaiShi, 2000-01-10",
                "xiaoShiTiCheng, 200"
        );
        checkCode(response, PPOK);

        // gt1x1 y1 2000-01-04 100
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + gt1x1Id,
                "yongHuId," + yongHu.getId(),
                "kaiShi, 2000-01-04",
                "xiaoShiTiCheng, 100"
        );
        checkCode(response, PPOK);

        // 导入工作记录
        jsonArray = new JSONArray();

        // y1 gt1x1 2000-01-04T10:00 2000-01-05T10:00 testNote3
        gongZuoJiLu = new PPJson();
        gongZuoJiLu.put("yongHuMing", "y1");
        gongZuoJiLu.put("xiangMuMingCheng", "gt1x1");
        gongZuoJiLu.put("kaiShi", "2000-01-04T10:00");
        gongZuoJiLu.put("jieShu", "2000-01-05T10:00");
        gongZuoJiLu.put("beiZhu", "testNote3");
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
       
        // 设置用户个人结算日 y1 2000-01-04
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-04"
        );
        checkCode(response, PPOK);
        // 添加提成
        // y1 2000-01-06 20
        response = request(
                "/admin/createTiCheng",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, y1",
                "riQi, 2000-01-06",
                "jinE, 20",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);

        // 生成y1 2000-01-02 到 2000-01-05的报告
        response = request(
                "/generateOwnYongHuBaoGao",
                HttpMethod.POST,
                "y1",
                "kaiShi, 2000-01-02",
                "jieShu, 2000-01-05"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray xmhz = ((JSONObject)jsonObject.get("data")).getJSONArray("项目汇总");
        Assert.assertEquals(2, xmhz.length());
        Assert.assertEquals("gt1x1", xmhz.getJSONObject(0).getString("项目"));
        Assert.assertEquals("gt1x2", xmhz.getJSONObject(1).getString("项目"));
        Assert.assertEquals(48, xmhz.getJSONObject(0).getInt("耗时"));
        Assert.assertEquals(1, xmhz.getJSONObject(1).getInt("耗时"));
        double qiChuBalance = jsonObject.getJSONObject("data").getDouble("期初Balance");
        double qiMoBalance = jsonObject.getJSONObject("data").getDouble("期末Balance");
        
        Assert.assertTrue(Math.abs(2 - qiChuBalance) < 0.0000001);
        Assert.assertTrue(Math.abs(3631 - qiMoBalance) < 0.0000001);
    }
}

package com.example.timesheet.admin.importYongHuGongZuoJiLu;

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
public class 简单成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.importYongHuGongZuoJiLu.JianDanChengGong";

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
        
        PPJson gongZuoJiLu3 = new PPJson();
        gongZuoJiLu3.put("yongHuMing", "y1");
        gongZuoJiLu3.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu3.put("kaiShi", "2000-01-04T00:00");
        gongZuoJiLu3.put("jieShu", "2000-01-05T10:00");
        gongZuoJiLu3.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);
        jsonArray.put(gongZuoJiLu2);
        jsonArray.put(gongZuoJiLu3);

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
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 2, 0, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 2, 1, 1, 0))
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
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 3, 0, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 4, 0, 0, 0))
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
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 4, 0, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 5, 0, 0, 0))
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
                                item.getKaiShi().isEqual(LocalDateTime.of(2000, 1, 5, 0, 0, 0))
                                &&
                                item.getJieShu().isEqual(LocalDateTime.of(2000, 1, 5, 10, 0, 0))
                                &&
                                item.getBeiZhu().equals("testNote")
                );

        Assert.assertTrue(result);
    }
}

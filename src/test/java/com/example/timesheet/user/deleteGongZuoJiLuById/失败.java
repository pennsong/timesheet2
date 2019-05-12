package com.example.timesheet.user.deleteGongZuoJiLuById;

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
public class 失败 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "user.deleteGongZuoJiLuById.ShiBai";

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
    public void 删除工作记录_$添加工作记录A_设置公司结算日为A日期_删除A() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 添加工作记录A
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-02-01T10:01");
        gongZuoJiLu1.put("jieShu", "2000-02-01T11:01");
        gongZuoJiLu1.put("beiZhu", "删除工作记录_$添加工作记录A_设置公司结算日为A日期_删除A1");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);
        Long gongZuoJiLuId = gongZuoJiLuRepository.findOneByBeiZhu("删除工作记录_$添加工作记录A_设置公司结算日为A日期_删除A1").getId();

        // 设置公司结算日
        response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + gongSi.getId(),
                "jieSuanRi, 2000-02-01"
        );
        checkCode(response, PPOK);

        // 删除结算日的工作记录
        response = request(
                "/deleteGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "y1"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Optional<GongZuoJiLu> gongZuoJiLuOptional = gongZuoJiLuRepository.findById(gongZuoJiLuId);
        Assert.assertTrue(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 删除工作记录_$添加工作记录A_设置公司结算日为A之后的日期_删除A() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 添加工作记录A
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-02-01T10:01");
        gongZuoJiLu1.put("jieShu", "2000-02-01T11:01");
        gongZuoJiLu1.put("beiZhu", "删除工作记录_$添加工作记录A_设置公司结算日为A之后的日期_删除A1");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);
        Long gongZuoJiLuId = gongZuoJiLuRepository.findOneByBeiZhu("删除工作记录_$添加工作记录A_设置公司结算日为A之后的日期_删除A1").getId();

        // 设置公司结算日
        response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + gongSi.getId(),
                "jieSuanRi, 2000-02-02"
        );
        checkCode(response, PPOK);

        // 删除结算日之前的工作记录
        response = request(
                "/deleteGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "y1"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Optional<GongZuoJiLu> gongZuoJiLuOptional = gongZuoJiLuRepository.findById(gongZuoJiLuId);
        Assert.assertTrue(gongZuoJiLuOptional.isPresent());
    }
    
    @Test
    public void 删除工作记录_$添加工作记录A_设置个人结算日为A日期_删除A() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 添加工作记录A
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-02-01T10:01");
        gongZuoJiLu1.put("jieShu", "2000-02-01T11:01");
        gongZuoJiLu1.put("beiZhu", "删除工作记录_$添加工作记录A_设置个人结算日为A日期_删除A");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);
        Long gongZuoJiLuId = gongZuoJiLuRepository.findOneByBeiZhu("删除工作记录_$添加工作记录A_设置个人结算日为A日期_删除A").getId();

        // 设置个人结算日
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + yongHu.getId(),
                "jieSuanRi, 2000-02-01"
        );
        checkCode(response, PPOK);

        // 删除结算日的工作记录
        response = request(
                "/deleteGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "y1"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Optional<GongZuoJiLu> gongZuoJiLuOptional = gongZuoJiLuRepository.findById(gongZuoJiLuId);
        Assert.assertTrue(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 删除工作记录_$添加工作记录A_设置个人结算日为A之后的日期_删除A() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");
        // 添加工作记录A
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-02-01T10:01");
        gongZuoJiLu1.put("jieShu", "2000-02-01T11:01");
        gongZuoJiLu1.put("beiZhu", "删除工作记录_$添加工作记录A_设置个人结算日为A之后的日期_删除A");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPOK);
        Long gongZuoJiLuId = gongZuoJiLuRepository.findOneByBeiZhu("删除工作记录_$添加工作记录A_设置个人结算日为A之后的日期_删除A").getId();

        // 设置个人结算日
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + yongHu.getId(),
                "jieSuanRi, 2000-02-02"
        );
        checkCode(response, PPOK);

        // 删除结算日之前的工作记录
        response = request(
                "/deleteGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "y1"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Optional<GongZuoJiLu> gongZuoJiLuOptional = gongZuoJiLuRepository.findById(gongZuoJiLuId);
        Assert.assertTrue(gongZuoJiLuOptional.isPresent());
    }
}

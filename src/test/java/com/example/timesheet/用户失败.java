package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.GongZuoJiLu;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 用户失败 extends TimesheetApplicationTests {
    private static boolean init = false;

    private static String dumpFileName = "yongHuShiBai";

    @Override
    public void initData() {

    }

    @BeforeTransaction
    void bt() {
        if (!init) {
            init = true;
            h2Service.restore("basicInitDB");
            initData();
            h2Service.dump(dumpFileName);

            // 获取登录cookies
            String cookie = login("Admin", "1234");
            jwts.put("Admin", cookie);

            for (int i = 1; i <= 3; i++) {
                cookie = login("y" + i, "1234");
                jwts.put("y" + i, cookie);
            }
        } else {
            h2Service.restore(dumpFileName);
        }
    }

    // 正式测试案例开始

    @Test
    public void 导入本人工作记录_开始时间大于结束时间() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T02:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        log.info(ppJson.toString());

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入本人工作记录_时间与已有同个用户的工作记录有重合1() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPOK);

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu2.put("jieShu", "2000-01-02T10:01");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入本人工作记录_时间与已有同个用户的工作记录有重合2() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPOK);

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T11:01");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:02");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入本人工作记录_时间与已有同个用户的工作记录有重合3() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPOK);

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:00");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入本人工作记录_时间与已有同个用户的工作记录有重合4() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPOK);

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:02");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入本人工作记录_项目名不存在() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "none");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPItemNotExistExceptionCode);
    }

    @Test
    public void 导入用户工作记录_用户不是项目成员() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y3");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        ResponseEntity<String> response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y3",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入本人工作记录_$设置公司结算日_开始时间等于项目所属公司结算日_开始时间小于项目所属公司结算日() {
        // 设置公司结算日
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 开始时间等于项目所属公司结算日
        PPJson gongZuoJiLu1 = new PPJson();

        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);

        // 开始时间小于项目所属公司结算日
        gongZuoJiLu1 = new PPJson();

        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-01T00:01");
        gongZuoJiLu1.put("jieShu", "2000-01-01T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/importGongZuoJiLu",
                HttpMethod.POST,
                "y1",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

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
}

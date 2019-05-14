package com.example.timesheet.user.importGongZuoJiLu;

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

    private static String dumpFileName = "user.importGongZuoJiLu.ShiBai";

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
    public void 导入本人工作记录_开始时间等于结束时间() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T02:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T02:01");
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
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:00");
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
    public void 导入本人工作记录_$设置个人结算日_开始时间等于个人结算日_开始时间小于个人结算日() {
        // 设置公司结算日
    	    YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 开始时间等于个人结算日
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

        // 开始时间小于个人结算日
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
}

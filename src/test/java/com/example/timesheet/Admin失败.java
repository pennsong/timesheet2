package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.GongZuoJiLu;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.util.PPJson;
import com.example.timesheet.util.PPUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;
import org.springframework.test.annotation.Commit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.example.timesheet.util.PPUtil.MIN_DATE;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Admin失败 extends TimesheetApplicationTests {
    private static int step = NOT_START;

    private static String dumpFileName = "adminShiBai";

    @Override
    public void initData() {
        // 如没有admin则新建admin
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
        if (yongHu == null) {
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal("500"), Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        /*
        用户
        y1 2
        y2 2
        y3 2
        */
        YongHu y1 = mainService.createYongHu("y1", "1234", new BigDecimal("2"));
        YongHu y2 = mainService.createYongHu("y2", "1234", new BigDecimal("2"));
        YongHu y3 = mainService.createYongHu("y3", "1234", new BigDecimal("2"));

       /*
       公司
       g1
       g2
       g3
       */
        GongSi g1 = mainService.createGongSi("g1");
        GongSi g2 = mainService.createGongSi("g2");
        GongSi g3 = mainService.createGongSi("g3");

        /*
        项目
        g1x1 g1
        [
            {
                y1,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ],
                y2,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ]
            }
        ]
        g1x2 g1
        g2x1 g2
        */
        XiangMu g1x1 = mainService.createXiangMu("g1x1", g1.getId());
        XiangMu g1x2 = mainService.createXiangMu("g1x2", g1.getId());
        XiangMu g2x1 = mainService.createXiangMu("g2x1", g2.getId());

        mainService.addXiangMuChengYuan(g1x1.getId(), y1.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));

        mainService.addXiangMuChengYuan(g1x1.getId(), y2.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));

        /*
        支付
        2000/1/1 g1 100.0 testNote
        */
        mainService.createZhiFu(g1.getMingCheng(), LocalDate.of(2000, 1, 1), new BigDecimal("100"), "testNote");

        /*
        workRecord
        g1x1 y1 2000/1/1 10:01 11:01 testWorkNote
        */
        mainService.createGongZuoJiLu(
                y1.getYongHuMing(),
                g1x1.getMingCheng(),
                LocalDateTime.of(2000, 1, 1, 10, 1),
                LocalDateTime.of(2000, 1, 1, 11, 1),
                "testWorkNote"
        );
    }

    @Before
    public void before() {
        if (step == NOT_START) {
            h2Service.restore("emptyDB");
        } else if (step == INIT_DATA_DONE) {
            h2Service.dump(dumpFileName);
        } else {
            h2Service.restore(dumpFileName);
        }
    }

    @Test
    @Commit
    public void _1initData() {
        initData();
        step = INIT_DATA_DONE;
    }

    @Test
    public void _2loginCookies() {
        // 获取登录cookies
        String cookie = login("Admin", "1234");
        cookies.put("Admin", cookie);

        for (int i = 1; i <= 3; i++) {
            cookie = login("y" + i, "1234");
            cookies.put("y" + i, cookie);
        }

        step = LOGIN_COOKIE_DONE;
    }

    // 正式测试案例开始

    @Test
    public void 新建用户_重名() {
        ResponseEntity<String> response = request(
                "/admin/createGongSi",
                HttpMethod.POST,
                "Admin",
                "mingCheng, g1"
        );
        checkCode(response, PPDuplicateExceptionCode);
    }

    @Test
    public void 删除用户_有分配到项目() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y2");

        ResponseEntity<String> response = request(
                "/admin/deleteYongHu/" + yongHu.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPReferencedExceptionCode);

        yongHu = yongHuRepository.findOneByYongHuMing("y2");
        Assert.assertNotNull(yongHu);
    }

    @Test
    public void 删除用户_有工作记录() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/deleteYongHu/" + yongHu.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPReferencedExceptionCode);

        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        Assert.assertNotNull(yongHu);
    }

    @Test
    public void 新建公司_重名() {
        ResponseEntity<String> response = request(
                "/admin/createGongSi",
                HttpMethod.POST,
                "Admin",
                "mingCheng, g1"
        );
        checkCode(response, PPDuplicateExceptionCode);
    }

    @Test
    public void 删除公司_有项目() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/deleteGongSi/" + gongSi.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPReferencedExceptionCode);

        gongSi = gongSiRepository.findOneByMingCheng("g1");
        Assert.assertNotNull(gongSi);
    }

    @Test
    public void 设置公司名称_重名() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/setGongSiMingCheng",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "mingCheng, g2"
        );
        checkCode(response, PPDuplicateExceptionCode);
    }

//    @Test
    // todo 这个要归入整体套路测试
//    public void 设置公司结算日_日期不合法() {
//        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");
//
//        PPJson ppJson = new PPJson();
//        ppJson.put("id", gongSi.getId());
//        ppJson.put("jieSuanRi", "2000-02-31");
//
//        HttpEntity<String> request = new HttpEntity<>(
//                ppJson.toString(),
//                headers
//        );
//
//        ResponseEntity<String> response = restTemplate.exchange("/admin/setGongSiJieSuanRi", HttpMethod.POST, request, String.class);
//        checkCode(response, PPValidateExceptionCode);
//    }

    @Test
    public void 新建项目_重名() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "mingCheng, g1x1",
                "gongSiId, " + gongSi.getId()
        );
        checkCode(response, PPDuplicateExceptionCode);
    }

    @Test
    public void 删除项目_有工作记录() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");

        ResponseEntity<String> response = request(
                "/admin/deleteXiangMu/" + xiangMu.getId(),
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPReferencedExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Assert.assertNotNull(xiangMu);
    }

    @Test
    public void 添加项目计费标准_$设置公司结算日_添加结算日的计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 设置公司结算日
        ResponseEntity<String> response = request(
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
                "kaiShi, 2000-01-02",
                "xiaoShiFeiYong, 501"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 添加项目计费标准_$设置公司结算日_添加结算日之前的计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 设置公司结算日
        ResponseEntity<String> response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 添加结算日之前的计费标准
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "xiaoShiFeiYong, 501"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 移除项目计费标准_$添加项目计费A_添加项目计费B_设置公司结算日为B的日期_移除A_移除B() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 添加项目计费A
        ResponseEntity<String> response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-01",
                "xiaoShiFeiYong, 500"
        );
        checkCode(response, PPOK);

        // 添加项目计费B
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-02",
                "xiaoShiFeiYong, 501"
        );
        checkCode(response, PPOK);

        // 设置公司结算日为A的日期
        response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + gongSi.getId(),
                "jieSuanRi, 2000-02-02"
        );
        checkCode(response, PPOK);

        // 移除A
        response = request(
                "/admin/removeXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-01"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 移除B
        response = request(
                "/admin/removeXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-02-02"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 移除项目计费标准_移除不存在的计费标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/removeXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-12-01"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 添加项目成员_成员已存在() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId()
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 移除项目成员_移除不存在的成员() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        ResponseEntity<String> response = request(
                "/admin/removeXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId()
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入用户工作记录_开始时间大于结束时间() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T02:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        log.info(ppJson.toString());

        ResponseEntity<String> response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入用户工作记录_时间与已有同个用户的工作记录有重合1() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu2.put("jieShu", "2000-01-02T10:01");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入用户工作记录_时间与已有同个用户的工作记录有重合2() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T11:01");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:02");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入用户工作记录_时间与已有同个用户的工作记录有重合3() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:00");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    public void 导入用户工作记录_时间与已有同个用户的工作记录有重合4() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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

        PPJson gongZuoJiLu2 = new PPJson();
        gongZuoJiLu2.put("yongHuMing", "y1");
        gongZuoJiLu2.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu2.put("kaiShi", "2000-01-02T10:00");
        gongZuoJiLu2.put("jieShu", "2000-01-02T11:02");
        gongZuoJiLu2.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu2);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入用户工作记录_用户名不存在() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "none");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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
        checkCode(response, PPItemNotExistExceptionCode);
    }

    @Test
    public void 导入用户工作记录_项目名不存在() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "none");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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
        checkCode(response, PPItemNotExistExceptionCode);
    }

    @Test
    public void 导入用户工作记录_用户不是项目成员() {
        PPJson gongZuoJiLu1 = new PPJson();
        gongZuoJiLu1.put("yongHuMing", "y3");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

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
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 导入用户工作记录_$设置公司结算日_开始时间等于项目所属公司结算日_开始时间小于项目所属公司结算日() {
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

        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-02T10:01");
        gongZuoJiLu1.put("jieShu", "2000-01-02T11:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        PPJson ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
                ppJson
        );
        checkCode(response, PPBusinessExceptionCode);

        // 开始时间小于项目所属公司结算日
        gongZuoJiLu1 = new PPJson();

        gongZuoJiLu1.put("yongHuMing", "y1");
        gongZuoJiLu1.put("xiangMuMingCheng", "g1x1");
        gongZuoJiLu1.put("kaiShi", "2000-01-01T00:01");
        gongZuoJiLu1.put("jieShu", "2000-01-01T01:01");
        gongZuoJiLu1.put("beiZhu", "testNote");

        jsonArray = new JSONArray();
        jsonArray.put(gongZuoJiLu1);

        ppJson = new PPJson();
        ppJson.put("data", jsonArray);

        response = request(
                "/admin/importYongHuGongZuoJiLu",
                HttpMethod.POST,
                "Admin",
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
                "/admin/deleteYongHuGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "Admin"
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
                "/admin/deleteYongHuGongZuoJiLu/" + gongZuoJiLuId,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        Optional<GongZuoJiLu> gongZuoJiLuOptional = gongZuoJiLuRepository.findById(gongZuoJiLuId);
        Assert.assertTrue(gongZuoJiLuOptional.isPresent());
    }

    @Test
    public void 新建支付_公司不存在() {
        ResponseEntity<String> response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, none",
                "riQi, 2000-03-01",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPItemNotExistExceptionCode);
    }
    
    @Test
    public void 新建支付_$设置公司结算日_新建结算日的支付_新建结算日之前的支付() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 设置公司结算日
        ResponseEntity<String> response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + gongSi.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 新建结算日的支付
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, g1",
                "riQi, 2000-01-02",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 新建结算日之前的支付
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, g1",
                "riQi, 2000-01-01",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 删除支付_$新建支付A_新建支付B_设置公司结算日为B的日期_删除A_删除B() {
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        // 新建支付A
        ResponseEntity<String> response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, g1",
                "riQi, 2000-01-02",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);
        Long idA = ppResponse.gainId(response);

        // 新建支付B
        response = request(
                "/admin/createZhiFu",
                HttpMethod.POST,
                "Admin",
                "gongSiMingCheng, g1",
                "riQi, 2000-01-03",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);
        Long idB = ppResponse.gainId(response);

        // 设置公司结算日
        response = request(
                "/admin/setGongSiJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + gongSi.getId(),
                "jieSuanRi, 2000-01-03"
        );
        checkCode(response, PPOK);

        // 删除A
        response = request(
                "/admin/deleteZhiFu/" + idA,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 删除B
        response = request(
                "/admin/deleteZhiFu/" + idB,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPBusinessExceptionCode);
    }
}

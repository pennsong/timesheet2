package com.example.timesheet.admin.addXiangMuTiChengBiaoZhun;

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

    private static String dumpFileName = "admin.addXiangMuTiChengBiaoZhun.ShiBai";

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
    public void 添加项目提成标准_$设置个人结算日_添加个人结算日当天开始的提成标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 设置个人结算日
        ResponseEntity<String> response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 添加个人结算日当天开始的提成标准
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-02",
                "xiaoShiTiCheng, 51"
        );
        checkCode(response, PPBusinessExceptionCode);
    }

    @Test
    public void 添加项目提成标准_$设置个人结算日_添加结算日之前的提成标准() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 设置公司结算日
        ResponseEntity<String> response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-02"
        );
        checkCode(response, PPOK);

        // 添加个人结算日之前的提成标准
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "xiaoShiTiCheng, 51"
        );
        checkCode(response, PPBusinessExceptionCode);
    }
}

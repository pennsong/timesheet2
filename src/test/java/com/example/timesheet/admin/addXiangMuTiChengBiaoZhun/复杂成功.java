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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 复杂成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.addXiangMuTiChengBiaoZhun.FuZaChengGong";

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
    public void 添加项目提成标准_$设置公司结算日A_设置个人结算日B_添加结算日B后的提成标准_添加同一天的提成标准() {
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
        
        // 设置用户个人结算日
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id, " + yongHu.getId(),
                "jieSuanRi, 2000-01-01"
        );
        checkCode(response, PPOK);

        // 添加用户结算日后的提成标准
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-02",
                "xiaoShiTiCheng, 51"
        );
        checkCode(response, PPOK);

        // 添加同一天的提成标准
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMu.getId(),
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-02",
                "xiaoShiTiCheng, 52"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getTiChengBiaoZhuns()
                .stream()
                .anyMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 1, 2))
                                        &&
                                        item.getXiaoShiTiCheng().compareTo(new BigDecimal("51")) == 0
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertFalse(result);

        result = xiangMu.getTiChengBiaoZhuns()
                .stream()
                .anyMatch(
                        item ->
                                item.getKaiShi().isEqual(LocalDate.of(2000, 1, 2))
                                        &&
                                        item.getXiaoShiTiCheng().compareTo(new BigDecimal("52")) == 0
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );

        Assert.assertTrue(result);
    }
}

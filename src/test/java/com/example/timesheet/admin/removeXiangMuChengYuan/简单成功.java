package com.example.timesheet.admin.removeXiangMuChengYuan;

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

    private static String dumpFileName = "admin.removeXiangMuChengYuan.JianDanCHengGong";

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
    public void 移除项目成员() {
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y2");

        ResponseEntity<String> response = request(
                "/admin/removeXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        Boolean result = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .noneMatch(
                        item ->
                                item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );
        Boolean result2 = xiangMu.getTiChengBiaoZhuns()
                .stream()
                .noneMatch(
                        item ->
                                item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                );
        Assert.assertTrue(result);
        Assert.assertTrue(result2);
    }
}

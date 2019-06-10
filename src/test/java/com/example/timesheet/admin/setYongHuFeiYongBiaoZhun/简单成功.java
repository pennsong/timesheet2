package com.example.timesheet.admin.setYongHuFeiYongBiaoZhun;

import com.example.timesheet.TimesheetApplicationTests;
import com.example.timesheet.model.YongHu;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.BeforeTransaction;

import java.math.BigDecimal;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 简单成功 extends TimesheetApplicationTests {

    private static boolean init = false;

    private static String dumpFileName = "admin.setYongHuFeiYongBiaoZhun.JianDanChengGong";

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
    public void 设置用户的小时费用和小时提成() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        ResponseEntity<String> response = request(
                "/admin/setYongHuFeiYongBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "yongHuId," + yongHu.getId(),
                "xiaoShiFeiYong, 10086",
                "xiaoShiTiCheng, 10010"
        );
        checkCode(response, PPOK);

        entityManager.clear();
        yongHu = yongHuRepository.findOneByYongHuMing("y1");
        Assert.assertEquals(yongHu.getXiaoShiFeiYong().compareTo(new BigDecimal("10086")), 0);
        Assert.assertEquals(yongHu.getXiaoShiTiCheng().compareTo(new BigDecimal("10010")), 0);
    }
}

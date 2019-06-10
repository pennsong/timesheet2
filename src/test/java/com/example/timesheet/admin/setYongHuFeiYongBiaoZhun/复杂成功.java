package com.example.timesheet.admin.setYongHuFeiYongBiaoZhun;

import com.example.timesheet.TimesheetApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.transaction.BeforeTransaction;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 复杂成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.setYongHuFeiYongBiaoZhun.FuZaChengGong";

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
    public void 测试() {
    	    Assert.assertEquals(1, 1);
    }
}

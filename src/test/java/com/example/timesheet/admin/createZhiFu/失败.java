package com.example.timesheet.admin.createZhiFu;

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

    private static String dumpFileName = "admin.createZhiFu.ShiBai";

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
}

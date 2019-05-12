package com.example.timesheet.admin.deleteZhiFuById;

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

    private static String dumpFileName = "admin.deleteZhiFuById.ShiBai";

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

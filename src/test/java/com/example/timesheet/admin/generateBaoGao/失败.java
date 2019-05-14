package com.example.timesheet.admin.generateBaoGao;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 失败 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.generateBaoGao.ShiBai";

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
    public void 生成报告_开始时间大于结束时间() throws JSONException{
    		GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/generateBaoGao",
                HttpMethod.POST,
                "Admin",
                "gongSiId, " + gongSi.getId(),
                "kaiShi, 2010-01-01",
                "jieShu, 2000-01-01",
                "setJieSuanRi, true"
        );
        checkCode(response, PPBusinessExceptionCode);
    }
}

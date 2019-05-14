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
public class 简单成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.deleteZhiFuById.JianDanChengGong";

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
    public void 删除支付() {
        Optional<ZhiFu> zhiFuOptional = StreamSupport.stream(zhiFuRepository.findAll().spliterator(), false).findFirst();

        Long id = zhiFuOptional.get().getId();

        ResponseEntity<String> response = request(
                "/admin/deleteZhiFu/" + id,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        zhiFuOptional = zhiFuRepository.findById(id);
        Assert.assertFalse(zhiFuOptional.isPresent());
    }
}

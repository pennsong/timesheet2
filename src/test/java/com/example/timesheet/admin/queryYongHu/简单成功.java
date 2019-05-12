package com.example.timesheet.admin.queryYongHu;

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

    private static String dumpFileName = "admin.queryYongHu.JianDanChengGong";

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
    public void 查询用户_有页码() throws JSONException {
    		ResponseEntity<String> response = request(
                "/admin/queryYongHu",
                HttpMethod.POST,
                "Admin",
                "size, 2",
                "page, 0"
        );
        checkCode(response, PPOK);
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(2, jsonObject.getJSONArray("data").length());
        Assert.assertEquals("y1", jsonObject.getJSONArray("data").getJSONObject(1).get("yongHuMing"));
        Assert.assertEquals(4, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(2, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
    }
    
    @Test
    public void 查询用户_无页码() throws JSONException {
    		ResponseEntity<String> response = request(
                "/admin/queryYongHu",
                HttpMethod.POST,
                "Admin"
        );
    		checkCode(response, PPOK);
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(4, jsonObject.getJSONArray("data").length());
        Assert.assertEquals("y1", jsonObject.getJSONArray("data").getJSONObject(1).get("yongHuMing"));
        Assert.assertEquals(4, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
    }
}

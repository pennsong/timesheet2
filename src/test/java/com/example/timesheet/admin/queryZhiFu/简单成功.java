package com.example.timesheet.admin.queryZhiFu;

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

    private static String dumpFileName = "admin.queryZhiFu.JianDanChengGong";

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
    public void 查询支付_有页码() throws JSONException {
    		ResponseEntity<String> response = request(
                "/admin/queryZhiFu",
                HttpMethod.POST,
                "Admin",
                "size, 10",
                "page, 0"
        );
        checkCode(response, PPOK);
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(1, jsonObject.getJSONArray("data").length());
        Assert.assertEquals("g1", jsonObject.getJSONArray("data").getJSONObject(0).get("gongSiObjMingCheng"));
        Assert.assertEquals("2000-01-01", jsonObject.getJSONArray("data").getJSONObject(0).get("riQi"));
        Assert.assertTrue(100 == jsonObject.getJSONArray("data").getJSONObject(0).getDouble("jinE"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
    }
    
    @Test
    public void 查询支付_无页码() throws JSONException {
    		ResponseEntity<String> response = request(
                "/admin/queryZhiFu",
                HttpMethod.POST,
                "Admin"
        );
    		checkCode(response, PPOK);
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(1, jsonObject.getJSONArray("data").length());
        Assert.assertEquals("g1", jsonObject.getJSONArray("data").getJSONObject(0).get("gongSiObjMingCheng"));
        Assert.assertEquals("2000-01-01", jsonObject.getJSONArray("data").getJSONObject(0).get("riQi"));
        Assert.assertTrue(100 == jsonObject.getJSONArray("data").getJSONObject(0).getDouble("jinE"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
    }
}

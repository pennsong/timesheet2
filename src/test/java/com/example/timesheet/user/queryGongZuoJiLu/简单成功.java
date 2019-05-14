package com.example.timesheet.user.queryGongZuoJiLu;

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

    private static String dumpFileName = "user.queryGongZuoJiLu.JianDanChengGong";

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
    public void 查询自己的工作记录_无任何参数限制() throws JSONException {
        ResponseEntity<String> response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("g1x1", jsonObject.getJSONArray("data").getJSONObject(0).get("xiangMuObjMingCheng"));
        Assert.assertEquals("y1", jsonObject.getJSONArray("data").getJSONObject(0).get("yongHuObjYongHuMing"));
        Assert.assertEquals("2000-01-01T10:01:00", jsonObject.getJSONArray("data").getJSONObject(0).get("kaiShi"));
        Assert.assertEquals("2000-01-01T11:01:00", jsonObject.getJSONArray("data").getJSONObject(0).get("jieShu"));
        Assert.assertEquals("testWorkNote", jsonObject.getJSONArray("data").getJSONObject(0).get("beiZhu"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
        
        response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y2"
        );
        checkCode(response, PPOK);
        jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(0, jsonObject.getJSONArray("data").length());
        Assert.assertTrue(jsonObject.getJSONObject("ppPageInfo").getBoolean("empty"));
    }
    
    @Test
    public void 查询自己的工作记录_有页码_有时间范围_有公司限制() throws JSONException {
    	    // 页码
    		ResponseEntity<String> response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1",
                "size, 10",
                "page, 0"
        );
        checkCode(response, PPOK);

        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("g1x1", jsonObject.getJSONArray("data").getJSONObject(0).get("xiangMuObjMingCheng"));
        Assert.assertEquals("2000-01-01T10:01:00", jsonObject.getJSONArray("data").getJSONObject(0).get("kaiShi"));
        Assert.assertEquals("testWorkNote", jsonObject.getJSONArray("data").getJSONObject(0).get("beiZhu"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getLong("totalElements"));
        Assert.assertEquals(1, jsonObject.getJSONObject("ppPageInfo").getInt("totalPages"));
        
        // 时间范围
        response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1",
                "kaiShi, 2000-02-01T00:00:00",
                "jieShu, 2000-02-02T00:00:00"
        );
        checkCode(response, PPOK);
        jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(0, jsonObject.getJSONArray("data").length());
        Assert.assertTrue(jsonObject.getJSONObject("ppPageInfo").getBoolean("empty"));
        
        // 公司限制
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g3");
        response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1",
                "gongSiId, " + gongSi.getId()
        );
        checkCode(response, PPOK);
        jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(0, jsonObject.getJSONArray("data").length());
        Assert.assertTrue(jsonObject.getJSONObject("ppPageInfo").getBoolean("empty"));
    }
    
    @Test
    public void 查询自己的工作记录_开始日期等于结束日期() throws JSONException{
    		ResponseEntity<String> response = request(
                "/queryGongZuoJiLu" ,
                HttpMethod.POST,
                "y1",
                "kaiShi, 2000-02-02T00:00:00",
                "jieShu, 2000-02-02T00:00:00"
        );
        checkCode(response, PPOK);
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals(0, jsonObject.getJSONArray("data").length());
        Assert.assertTrue(jsonObject.getJSONObject("ppPageInfo").getBoolean("empty"));
    }
}

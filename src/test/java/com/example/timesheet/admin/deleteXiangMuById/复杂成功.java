package com.example.timesheet.admin.deleteXiangMuById;

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
public class 复杂成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.deleteXiangMuById.FuZaChengGong";

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
    public void 删除项目_$新建项目_添加成员_添加计费标准_添加提成标准_删除项目() {
        // 新建项目
        GongSi gongSi = gongSiRepository.findOneByMingCheng("g1");

        ResponseEntity<String> response = request(
                "/admin/createXiangMu",
                HttpMethod.POST,
                "Admin",
                "mingCheng, g1xt1",
                "gongSiId, " + gongSi.getId()
        );
        checkCode(response, PPOK);

        // 添加成员
        Long xiangMuId = ppResponse.gainId(response);
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMuId,
                "yongHuId, " + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 添加计费标准
        response = request(
                "/admin/addXiangMuJiFeiBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMuId,
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "xiaoShiFeiYong, 1.1"
        );
        checkCode(response, PPOK);
        
        // 添加提成标准
        response = request(
                "/admin/addXiangMuTiChengBiaoZhun",
                HttpMethod.POST,
                "Admin",
                "xiangMuId, " + xiangMuId,
                "yongHuId, " + yongHu.getId(),
                "kaiShi, 2000-01-01",
                "xiaoShiTiCheng, 1"
        );
        checkCode(response, PPOK);

        // 删除项目
        response = request(
                "/admin/deleteXiangMu/" + xiangMuId,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        gongSi = gongSiRepository.findOneByMingCheng("g1xt1");
        Assert.assertNull(gongSi);
    }
}

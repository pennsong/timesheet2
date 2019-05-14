package com.example.timesheet.admin.deleteTiChengById;

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

    private static String dumpFileName = "admin.deleteTiChengById.ShiBai";

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
    public void 删除提成_$新建提成A_A先_新建提成B_B后_设置个人结算日为B的日期_删除A_删除B() {
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y1");

        // 新建提成A
        ResponseEntity<String> response = request(
                "/admin/createTiCheng",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, y1",
                "riQi, 2000-01-02",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);
        Long idA = ppResponse.gainId(response);

        // 新建提成B
        response = request(
                "/admin/createTiCheng",
                HttpMethod.POST,
                "Admin",
                "yongHuMing, y1",
                "riQi, 2000-01-03",
                "jinE, 1.1",
                "beiZhu, testPayment"
        );
        checkCode(response, PPOK);
        Long idB = ppResponse.gainId(response);

        // 设置个人结算日
        response = request(
                "/admin/setYongHuJieSuanRi",
                HttpMethod.POST,
                "Admin",
                "id," + yongHu.getId(),
                "jieSuanRi, 2000-01-03"
        );
        checkCode(response, PPOK);

        // 删除A
        response = request(
                "/admin/deleteTiCheng/" + idA,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPBusinessExceptionCode);

        // 删除B
        response = request(
                "/admin/deleteTiCheng/" + idB,
                HttpMethod.DELETE,
                "Admin"
        );
        checkCode(response, PPBusinessExceptionCode);
    }
}

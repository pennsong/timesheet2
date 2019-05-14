package com.example.timesheet.admin.addXiangMuChengYuan;

import static com.example.timesheet.util.PPUtil.MIN_DATE;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.example.timesheet.TimesheetApplicationTests;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class 简单成功 extends TimesheetApplicationTests {
	private static boolean init = false;

    private static String dumpFileName = "admin.addXiangMuChengYuan.JianDanChengGong";

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
    public void 添加项目成员() {
        log.info("pptest: 添加项目成员1");
        XiangMu xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("y3");

        ResponseEntity<String> response = request(
                "/admin/addXiangMuChengYuan",
                HttpMethod.POST,
                "Admin",
                "xiangMuId," + xiangMu.getId(),
                "yongHuId," + yongHu.getId()
        );
        checkCode(response, PPOK);

        // 清空当前repository以从数据库获取最新数据
        entityManager.clear();

        xiangMu = xiangMuRepository.findOneByMingCheng("g1x1");
        long count = xiangMu.getJiFeiBiaoZhuns()
                .stream()
                .filter(
                        item ->
                                item.getKaiShi().isEqual(MIN_DATE)
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                                        &&
                                        item.getXiaoShiFeiYong().compareTo(yongHu.getXiaoShiFeiYong()) == 0
                ).count();
        long count2 = xiangMu.getTiChengBiaoZhuns()
                .stream()
                .filter(
                        item ->
                                item.getKaiShi().isEqual(MIN_DATE)
                                        &&
                                        item.getYongHu().getId().compareTo(yongHu.getId()) == 0
                                        &&
                                        item.getXiaoShiTiCheng().compareTo(yongHu.getXiaoShiTiCheng()) == 0
                ).count();

        log.info("pptest: 添加项目成员2");
        Assert.assertTrue(count == 1);
        Assert.assertTrue(count2 == 1);
    }
}

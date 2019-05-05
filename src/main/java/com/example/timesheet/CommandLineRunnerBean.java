package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.GongSiRepository;
import com.example.timesheet.repository.GongZuoJiLuRepository;
import com.example.timesheet.repository.XiangMuRepository;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.service.H2Service;
import com.example.timesheet.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@Transactional
public class CommandLineRunnerBean implements CommandLineRunner {
    @Autowired
    private H2Service h2Service;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MainService mainService;

    @Autowired
    private GongSiRepository gongSiRepository;

    @Autowired
    private XiangMuRepository xiangMuRepository;

    @Autowired
    private YongHuRepository yongHuRepository;

    @Autowired
    private GongZuoJiLuRepository gongZuoJiLuRepository;

    @Override
    public void run(String... args) {
        // 如没有admin则新建admin
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
        if (yongHu == null) {
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal("500"), Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        /*
        用户
        y1 2
        y2 2
        y3 2
        */
        YongHu y1 = mainService.createYongHu("y1", "1234", new BigDecimal("2"));
        YongHu y2 = mainService.createYongHu("y2", "1234", new BigDecimal("2"));
        YongHu y3 = mainService.createYongHu("y3", "1234", new BigDecimal("2"));

       /*
       公司
       g1
       g2
       g3
       */
        GongSi g1 = mainService.createGongSi("g1");
        GongSi g2 = mainService.createGongSi("g2");
        GongSi g3 = mainService.createGongSi("g3");

        /*
        项目
        g1x1 g1
        [
            {
                y1,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ],
                y2,
                xiaoShiFeiYong: [
                    {
                        MIN_DATE,
                        2
                    },
                    {
                        2000/1/1,
                        4
                    }
                ]
            }
        ]
        g1x2 g1
        g2x1 g2
        */
        XiangMu g1x1 = mainService.createXiangMu("g1x1", g1.getId());
        XiangMu g1x2 = mainService.createXiangMu("g1x2", g1.getId());
        XiangMu g2x1 = mainService.createXiangMu("g2x1", g2.getId());

        mainService.addXiangMuChengYuan(g1x1.getId(), y1.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y1.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));

        mainService.addXiangMuChengYuan(g1x1.getId(), y2.getId());
        mainService.addXiangMuJiFeiBiaoZhun(g1x1.getId(), y2.getId(), LocalDate.of(2000, 1, 1), new BigDecimal("4"));

        /*
        支付
        2000/1/1 g1 100.0 testNote
        */
        mainService.createZhiFu(g1.getMingCheng(), LocalDate.of(2000, 1, 1), new BigDecimal("100"), "testNote");

        /*
        workRecord
        g1x1 y1 2000/1/1 10:01 11:01 testWorkNote
        */
        mainService.createGongZuoJiLu(
                y1.getYongHuMing(),
                g1x1.getMingCheng(),
                LocalDateTime.of(2000, 1, 1, 10, 1),
                LocalDateTime.of(2000, 1, 1, 11, 1),
                "testWorkNote"
        );

        h2Service.dump("basicInitDB");
        log.info("init ok");
    }
}
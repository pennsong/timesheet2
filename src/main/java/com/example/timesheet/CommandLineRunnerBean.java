package com.example.timesheet;

import com.example.timesheet.controller.MainController;
import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.GongSiRepository;
import com.example.timesheet.repository.GongZuoJiLuRepository;
import com.example.timesheet.repository.XiangMuRepository;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.service.MainService;
import com.example.timesheet.util.PPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@Transactional
public class CommandLineRunnerBean implements CommandLineRunner {
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
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1"), new BigDecimal(500), Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        /*user
        u1 2 0
        u2 2 1
        u3 2 1
        */
        YongHu u1 = mainService.createYongHu("u1", "1234", new BigDecimal(500));
        YongHu u2 = mainService.createYongHu("u2", "1234", new BigDecimal(500));
        YongHu u3 = mainService.createYongHu("u3", "1234", new BigDecimal(500));

       /* company
       c1
       c2
       c3
       c4
       c5
       c6
       */
        GongSi c1 = mainService.createGongSi("c1");
        GongSi c2 = mainService.createGongSi("c2");
        GongSi c3 = mainService.createGongSi("c3");
        GongSi c4 = mainService.createGongSi("c4");
        GongSi c5 = mainService.createGongSi("c5");
        GongSi c6 = mainService.createGongSi("c6");

        /*
        project
        c1p1 c1 worker(u1, u2)
        [
            {
                u1,
                hourCost: [
                    {
                        1999/12/31,
                        2
                    },
                    {
                        2000/1/5,
                        4
                    },
                    {
                        2000/1/20,
                        6
                    },
                    {
                        2099/1/1,
                        8
                    },
                ],
                hourCommission: [
                    {
                        1999/12/31,
                        1
                    },
                    {
                        2000/1/5,
                        2
                    },
                    {
                        2000/1/20,
                        3
                    },
                    {
                        2099/1/1,
                        4
                    },
                ],
            },
            {
                u2,
                hourCost: [
                    {
                        1999/12/31,
                        2
                    }
                ],
                hourCommission: [
                    {
                        1999/12/31,
                        1
                    }
                ]
            }
        ]
        c1p2 c1
        c2p1 c2
        c5p1 c5 worker(u1)
        c6p1 c6 worker(u1)
        [
            {
                u1,
                hourCost: [
                    {
                        1999/12/31,
                        2
                    }
                ],
                hourCommission: [
                    {
                        1999/12/31,
                        1
                    }
                ]
            }
        ]
        */
        XiangMu c1p1 = mainService.createXiangMu("c1p1", c1.getId());
        XiangMu c1p2 = mainService.createXiangMu("c1p2", c1.getId());
        XiangMu c2p1 = mainService.createXiangMu("c2p1", c2.getId());
        XiangMu c5p1 = mainService.createXiangMu("c5p1", c5.getId());
        XiangMu c6p1 = mainService.createXiangMu("c6p1", c6.getId());

        mainService.addXiangMuChengYuan(c5p1.getId(), u1.getId());
        mainService.addXiangMuChengYuan(c6p1.getId(), u1.getId());

        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(1999, 12, 31), new BigDecimal(2));
        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(2000, 1, 5), new BigDecimal(4));
        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(2000, 1, 20), new BigDecimal(6));
        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(2099, 1, 1), new BigDecimal(8));
        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(1999, 12, 31), new BigDecimal(2));
        mainService.addXiangMuJiFeiBiaoZhun(c1p1.getId(), u1.getId(), LocalDate.of(1999, 12, 31), new BigDecimal(2));

        /*
        payment
        1999/12/1 c1 100.0 testNote
        2000/1/5 c1 100.0 testNote
        2000/1/15 c1 100.0 testNote
        2000/1/15 c4 100.0 testNote
        */
        mainService.createZhiFu(c1.getMingCheng(), LocalDate.of(1999, 12, 1), new BigDecimal(100), "testNote");
        mainService.createZhiFu(c1.getMingCheng(), LocalDate.of(2000, 1, 5), new BigDecimal(100), "testNote");
        mainService.createZhiFu(c1.getMingCheng(), LocalDate.of(2000, 1, 15), new BigDecimal(100), "testNote");
        mainService.createZhiFu(c4.getMingCheng(), LocalDate.of(2000, 1, 15), new BigDecimal(100), "testNote");

        /*
        workRecord
        c1p1 u1 2000/1/1 10:01 11:01 testWorkNote
        c1p1 u1 2000/1/5 10:01 11:01 testWorkNote
        c1p1 u1 2000/1/6 10:01 11:01 testWorkNote
        */
        mainService.createGongZuoJiLu(
                u1.getYongHuMing(),
                c1p1.getMingCheng(),
                LocalDateTime.of(2000, 1, 1, 10, 1),
                LocalDateTime.of(2000, 1, 1, 11, 1),
                "testWorkNote"
        );
        mainService.createGongZuoJiLu(
                u1.getYongHuMing(),
                c1p1.getMingCheng(),
                LocalDateTime.of(2000, 1, 5, 10, 1),
                LocalDateTime.of(2000, 1, 5, 11, 1),
                "testWorkNote"
        );
        mainService.createGongZuoJiLu(
                u1.getYongHuMing(),
                c1p1.getMingCheng(),
                LocalDateTime.of(2000, 1, 6, 10, 1),
                LocalDateTime.of(2000, 1, 6, 11, 1),
                "testWorkNote"
        );
    }
}
package com.example.timesheet;

import com.example.timesheet.model.GongSi;
import com.example.timesheet.model.XiangMu;
import com.example.timesheet.model.YongHu;
import com.example.timesheet.repository.GongSiRepository;
import com.example.timesheet.repository.GongZuoJiLuRepository;
import com.example.timesheet.repository.XiangMuRepository;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.service.DBService;
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
    private DBService h2Service;

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
    public void run(String... args) throws InterruptedException {
        h2Service.dump("emptyDB");
        log.info("dump emptyDB ok");
        // 如没有admin则新建admin
        YongHu yongHu = yongHuRepository.findOneByYongHuMing("Admin");
        if (yongHu == null) {
            YongHu yongHu1 = new YongHu(null, "Admin", passwordEncoder.encode("1234"), new BigDecimal("500"), Arrays.asList("ADMIN"));
            yongHuRepository.save(yongHu1);
        }

        YongHu y1 = mainService.createYongHu("Penn", "1234", new BigDecimal("500"));
        YongHu y2 = mainService.createYongHu("Jimi", "1234", new BigDecimal("500"));
        YongHu y3 = mainService.createYongHu("Jay", "1234", new BigDecimal("500"));
        YongHu y4 = mainService.createYongHu("Tracy", "1234", new BigDecimal("500"));
        YongHu y5 = mainService.createYongHu("Jin", "1234", new BigDecimal("500"));
        YongHu y6 = mainService.createYongHu("Fan", "1234", new BigDecimal("500"));

        GongSi g1 = mainService.createGongSi("立派");
        GongSi g2 = mainService.createGongSi("量子健康");
        GongSi g3 = mainService.createGongSi("Harvey公司");
        GongSi g4 = mainService.createGongSi("磐哲");

    }
}
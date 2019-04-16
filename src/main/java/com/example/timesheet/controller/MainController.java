package com.example.timesheet.controller;

import com.example.timesheet.aop.DtoValid;
import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.exception.PPValidateException;
import com.example.timesheet.model.*;
import com.example.timesheet.repository.GongSiRepository;
import com.example.timesheet.repository.XiangMuRepository;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.service.MainService;
import com.example.timesheet.util.PPResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(consumes = "application/json", produces = "application/json")
@Transactional
public class MainController {
    @Autowired
    MainService mainService;

    @Autowired
    private GongSiRepository gongSiRepository;

    @Autowired
    private XiangMuRepository xiangMuRepository;

    @Autowired
    private YongHuRepository yongHuRepository;

    @RequestMapping("/homepage")
    public String homepage() {

        return "homepage";
    }

    @RequestMapping("/test")
    public String test(Principal principal) {

        log.info(principal.toString());

        return "homepage";
    }

    // -Admin

    /**
     * 新建用户
     */
    @RequestMapping(value = "/admin/createYongHu", method = RequestMethod.POST)
    @DtoValid
    public String createYongHu(@RequestBody CreateYongHuDto dto) {
        mainService.createYongHu(dto.yongHuMing, dto.password, dto.hourCost);

        return PPResponse.response("ok");
    }

    @Data
    private static class CreateYongHuDto {
        @NotEmpty
        @Size(min = 2)
        String yongHuMing;

        @NotEmpty
        @Size(min = 4)
        String password;

        @NotNull
        @Positive
        BigDecimal hourCost;
    }

    /**
     * 删除用户
     */
    @RequestMapping(value = "/admin/deleteYongHu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteYongHu(@PathVariable Long id) {
        mainService.deleteYongHu(id);

        return PPResponse.response("ok");
    }

    /**
     * 设置指定用户密码
     */
    @RequestMapping(value = "/admin/setYongHuPassword", method = RequestMethod.POST)
    @DtoValid
    public String setYongHuPassword(@RequestBody YongHuPasswordDto dto) {
        mainService.changePassword(dto.yongHuId, dto.password);

        return PPResponse.response("ok");
    }

    @Data
    private static class YongHuPasswordDto {
        @NotNull
        Long yongHuId;

        @NotEmpty
        @Size(min = 4)
        String password;
    }

    /**
     * 新建公司
     */
    @RequestMapping(value = "/admin/createGongSi", method = RequestMethod.POST)
    @DtoValid
    public String createGongSi(@RequestBody CreateGongSiDto dto) {
        mainService.createGongSi(dto.mingCheng);

        return PPResponse.response("ok");
    }

    @Data
    private static class CreateGongSiDto {
        @NotEmpty
        String mingCheng;
    }

    /**
     * 删除用户
     */
    @RequestMapping(value = "/admin/deleteGongSi/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteGongSi(@PathVariable Long id) {
        mainService.deleteGongSi(id);

        return PPResponse.response("ok");
    }

    /**
     * 设置公司名称
     */
    @RequestMapping(value = "/admin/setGongSiMingCheng", method = RequestMethod.POST)
    @DtoValid
    public String setGongSiMingCheng(@RequestBody SetGongSiMingChengDto dto) {
        mainService.setGongSiMingCheng(dto.id, dto.mingCheng);

        return PPResponse.response("ok");
    }

    @Data
    private static class SetGongSiMingChengDto {
        @NotNull
        Long id;

        @NotEmpty
        String mingCheng;
    }

    /**
     * 设置公司结算日
     */
    @RequestMapping(value = "/admin/setGongSiJieSuanRi", method = RequestMethod.POST)
    @DtoValid
    public String setGongSiJieSuanRi(@RequestBody SetGongSiJieSuanRiDto dto) {
        mainService.setGongSiJieSuanRi(dto.id, dto.jieSuanRi);

        return PPResponse.response("ok");
    }

    @Data
    private static class SetGongSiJieSuanRiDto {
        @NotNull
        Long id;

        @NotNull
        LocalDate jieSuanRi;
    }

    /**
     * 新建项目
     */
    @RequestMapping(value = "/admin/createXiangMu", method = RequestMethod.POST)
    @DtoValid
    public String createXiangMu(@RequestBody createXiangMuDto dto) {
        mainService.createXiangMu(dto.mingCheng, dto.gongSiId);

        return PPResponse.response("ok");
    }

    @Data
    private static class createXiangMuDto {
        @NotEmpty
        String mingCheng;

        @NotNull
        Long gongSiId;
    }

    /**
     * 删除项目
     */
    @RequestMapping(value = "/admin/deleteXiangMu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteXiangMu(@PathVariable Long id) {
        mainService.deleteXiangMu(id);

        return PPResponse.response("ok");
    }

    /**
     * 添加项目计费标准
     */
    @RequestMapping(value = "/admin/addXiangMuJiFeiBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public String addXiangMuJiFeiBiaoZhun(@RequestBody AddXiangMuJiFeiBiaoZhunDto dto) {
        mainService.addXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi, dto.xiaoShiFeiYong);

        return PPResponse.response("ok");
    }

    @Data
    private static class AddXiangMuJiFeiBiaoZhunDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        BigDecimal xiaoShiFeiYong;
    }

    /**
     * 移除项目计费标准
     */
    @RequestMapping(value = "/admin/removeXiangMuJiFeiBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public String removeXiangMuJiFeiBiaoZhun(@RequestBody RemoveXiangMuJiFeiBiaoZhun dto) {
        mainService.removeXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi);

        return PPResponse.response("ok");
    }

    @Data
    private static class RemoveXiangMuJiFeiBiaoZhun {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;
    }

    /**
     * 添加项目成员
     */
    @RequestMapping(value = "/admin/addXiangMuChengYuan", method = RequestMethod.POST)
    @DtoValid
    public String addXiangMuChengYuan(@RequestBody AddXiangMuChengYuan dto) {
        mainService.addXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPResponse.response("ok");
    }

    @Data
    private static class AddXiangMuChengYuan {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

    /**
     * 移除项目成员
     */
    @RequestMapping(value = "/admin/removeXiangMuChengYuan", method = RequestMethod.POST)
    @DtoValid
    public String removeXiangMuChengYuan(@RequestBody RemoveXiangMuChengYuan dto) {
        mainService.addXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPResponse.response("ok");
    }

    @Data
    private static class RemoveXiangMuChengYuan {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

    /**
     * 导入用户工作记录
     */
    @RequestMapping(value = "/admin/importYongHuGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public String importYongHuGongZuoJiLu(@RequestBody ImportYongHuGongZuoJiLuDto dto) {
        for (YongHuGongZuoJiLuDto item : dto.data) {
            mainService.createGongZuoJiLu(item.yongHuMing,
                    item.xiangMuMingCheng,
                    item.kaiShi,
                    item.jieShu,
                    item.beiZhu);
        }

        return PPResponse.response("ok");
    }

    @Data
    private static class ImportYongHuGongZuoJiLuDto {
        @NotEmpty
        @Valid
        List<YongHuGongZuoJiLuDto> data;
    }

    @Data
    private static class YongHuGongZuoJiLuDto {
        @NotEmpty
        String yongHuMing;

        @NotEmpty
        String xiangMuMingCheng;

        @NotNull
        LocalDateTime kaiShi;

        @NotNull
        LocalDateTime jieShu;

        @NotEmpty
        String beiZhu;
    }

    /**
     * 删除工作记录
     */
    @RequestMapping(value = "/admin/deleteYongHuGongZuoJiLu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteYongHuGongZuoJiLu(@PathVariable Long id) {
        mainService.deleteGongZuoJiLu(id);

        return PPResponse.response("ok");
    }

    /**
     * 新建支付
     */
    @RequestMapping(value = "/admin/createZhiFu", method = RequestMethod.POST)
    @DtoValid
    public String createZhiFu(@RequestBody CreateZhiFuDto dto) {
        mainService.createZhiFu(dto.gongSiMingCheng, dto.riQi, dto.jinE, dto.beiZhu);

        return PPResponse.response("ok");
    }

    @Data
    private static class CreateZhiFuDto {
        @NotEmpty
        String gongSiMingCheng;

        @NotNull
        LocalDate riQi;

        @NotNull
        BigDecimal jinE;

        String beiZhu;
    }

    /**
     * 删除支付
     */
    @RequestMapping(value = "/admin/deleteZhiFu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteZhiFu(@PathVariable Long id) {
        mainService.deleteZhiFu(id);

        return PPResponse.response("ok");
    }

    /**
     * 生成报告
     * <p>
     * 1) 成功生成报告后, 把对应公司的结算日设置为报告结束日期
     */
    @RequestMapping(value = "/admin/generateBaoGao", method = RequestMethod.POST)
    @DtoValid
    public String generateBaoGao(@RequestBody GenerateBaoGaoDto dto) {
        JSONObject report;

        try {
            report = mainService.generateBaoGao(dto.gongSiId, dto.kaiShi, dto.jieShu);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        // 成功生成报告后, 把对应公司的结算日设置为报告结束日期
        GongSi gongSi = mainService.gainEntityWithExistsChecking(GongSi.class, dto.gongSiId);
        gongSi.setJieSuanRi(dto.jieShu);

        return PPResponse.response(report);
    }

    @Data
    private static class GenerateBaoGaoDto {
        @NotNull
        Long gongSiId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        LocalDate jieShu;
    }
    // -

    /**
     * 设置当前用户密码
     */
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @DtoValid
    public String changePassword(Authentication authentication, @RequestBody PasswordDto dto) {
        Long yongHuId = ((YongHu) authentication.getPrincipal()).getId();

        mainService.changePassword(yongHuId, dto.password);

        return PPResponse.response("ok");
    }

    @Data
    private static class PasswordDto {
        @NotEmpty
        @Size(min = 4)
        String password;
    }

    /**
     * 导入本人工作记录
     */
    @RequestMapping(value = "/importGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public String importGongZuoJiLu(Authentication authentication, @RequestBody ImportGongZuoJiLuDto dto) {
        for (GongZuoJiLuDto item : dto.data) {
            String yongHuMing = ((YongHu) authentication.getPrincipal()).getYongHuMing();

            mainService.createGongZuoJiLu(yongHuMing,
                    item.xiangMuMingCheng,
                    item.kaiShi,
                    item.jieShu,
                    item.beiZhu);
        }

        return PPResponse.response("ok");
    }

    @Data
    private static class ImportGongZuoJiLuDto {
        @NotEmpty
        @Valid
        List<GongZuoJiLuDto> data;
    }

    @Data
    private static class GongZuoJiLuDto {
        @NotEmpty
        String xiangMuMingCheng;

        @NotNull
        LocalDateTime kaiShi;

        @NotNull
        LocalDateTime jieShu;

        @NotEmpty
        String beiZhu;
    }

    /**
     * 删除本人工作记录
     */
    @RequestMapping(value = "/admin/deleteGongZuoJiLu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteGongZuoJiLu(@PathVariable Long id) {
        GongZuoJiLu gongZuoJiLu = mainService.gainEntityWithExistsChecking(GongZuoJiLu.class, id);

        if (gongZuoJiLu.getYongHu().getId() != id) {
            throw new PPBusinessException("只能删除本人的工作记录!");
        }

        mainService.deleteGongZuoJiLu(id);

        return PPResponse.response("ok");
    }
}

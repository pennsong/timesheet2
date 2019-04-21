package com.example.timesheet.controller;

import com.example.timesheet.aop.DtoValid;
import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.exception.PPValidateException;
import com.example.timesheet.model.*;
import com.example.timesheet.repository.GongSiRepository;
import com.example.timesheet.repository.XiangMuRepository;
import com.example.timesheet.repository.YongHuRepository;
import com.example.timesheet.service.MainService;
import com.example.timesheet.service.PPResponse;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;
import java.util.List;

import static com.example.timesheet.util.PPUtil.MAX_DATE;
import static com.example.timesheet.util.PPUtil.MIN_DATE;

@Slf4j
@RestController
//@RequestMapping(consumes = "application/json", produces = "application/json")
@Transactional(propagation = Propagation.REQUIRES_NEW)
@Api(value = "主Controller", description = "主Controller")
public class MainController {

    @Autowired
    private PPResponse ppResponse;

    @Autowired
    private MainService mainService;

    @Autowired
    private GongSiRepository gongSiRepository;

    @Autowired
    private XiangMuRepository xiangMuRepository;

    @Autowired
    private YongHuRepository yongHuRepository;

    // -Admin

    @ApiOperation(value = "新建用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/createYongHu", method = RequestMethod.POST)
    @DtoValid
    public String createYongHu(@RequestBody CreateYongHuDto dto) {
        YongHu yongHu = mainService.createYongHu(dto.yongHuMing, dto.miMa, dto.xiaoShiFeiYong);

        return ppResponse.response(yongHu);
    }

    @ApiModel(description = "新建用户Dto")
    @Data
    public static class CreateYongHuDto {
        @ApiModelProperty(notes = "用户名", required = true, position = 1)
        @NotBlank
        @Size(min = 2)
        String yongHuMing;

        @ApiModelProperty(notes = "密码", required = true, position = 2)
        @NotNull
        @Size(min = 4)
        String miMa;

        @ApiModelProperty(notes = "小时费用", required = true, position = 3)
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal xiaoShiFeiYong;
    }

    @ApiOperation(value = "删除用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/deleteYongHu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteYongHu(@PathVariable Long id) {
        mainService.deleteYongHu(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "设置指定用户密码", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/setYongHuPassword", method = RequestMethod.POST)
    @DtoValid
    public String setYongHuPassword(@RequestBody YongHuPasswordDto dto) {
        mainService.changePassword(dto.yongHuId, dto.password);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class YongHuPasswordDto {
        @NotNull
        Long yongHuId;

        @NotBlank
        @Size(min = 4)
        String password;
    }

    @ApiOperation(value = "新建公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/createGongSi", method = RequestMethod.POST)
    @DtoValid
    public String createGongSi(@RequestBody CreateGongSiDto dto) {
        GongSi gongSi = mainService.createGongSi(dto.mingCheng);

        return ppResponse.response(gongSi);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CreateGongSiDto {
        @NotBlank
        String mingCheng;
    }

    @ApiOperation(value = "删除公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/deleteGongSi/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteGongSi(@PathVariable Long id) {
        mainService.deleteGongSi(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "设置公司名称", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/setGongSiMingCheng", method = RequestMethod.POST)
    @DtoValid
    public String setGongSiMingCheng(@RequestBody SetGongSiMingChengDto dto) {
        mainService.setGongSiMingCheng(dto.id, dto.mingCheng);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SetGongSiMingChengDto {
        @NotNull
        Long id;

        @NotBlank
        String mingCheng;
    }

    @ApiOperation(value = "设置公司结算日", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/setGongSiJieSuanRi", method = RequestMethod.POST)
    @DtoValid
    public String setGongSiJieSuanRi(@RequestBody SetGongSiJieSuanRiDto dto) {
        mainService.setGongSiJieSuanRi(dto.id, dto.jieSuanRi);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SetGongSiJieSuanRiDto {
        @NotNull
        Long id;

        @NotNull
        LocalDate jieSuanRi;
    }

    @ApiOperation(value = "新建项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/createXiangMu", method = RequestMethod.POST)
    @DtoValid
    public String createXiangMu(@RequestBody createXiangMuDto dto) {
        XiangMu xiangMu = mainService.createXiangMu(dto.mingCheng, dto.gongSiId);

        return ppResponse.response(xiangMu);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class createXiangMuDto {
        @NotBlank
        String mingCheng;

        @NotNull
        Long gongSiId;
    }

    @ApiOperation(value = "删除项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/deleteXiangMu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteXiangMu(@PathVariable Long id) {
        mainService.deleteXiangMu(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "添加项目计费标准", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/addXiangMuJiFeiBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public String addXiangMuJiFeiBiaoZhun(@RequestBody AddXiangMuJiFeiBiaoZhunDto dto) {
        mainService.addXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi, dto.xiaoShiFeiYong);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AddXiangMuJiFeiBiaoZhunDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        BigDecimal xiaoShiFeiYong;
    }

    @ApiOperation(value = "移除项目计费标准", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/removeXiangMuJiFeiBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public String removeXiangMuJiFeiBiaoZhun(@RequestBody RemoveXiangMuJiFeiBiaoZhun dto) {
        mainService.removeXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RemoveXiangMuJiFeiBiaoZhun {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;
    }

    @ApiOperation(value = "添加项目成员", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/addXiangMuChengYuan", method = RequestMethod.POST)
    @DtoValid
    public String addXiangMuChengYuan(@RequestBody AddXiangMuChengYuan dto) {
        mainService.addXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AddXiangMuChengYuan {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

    @ApiOperation(value = "移除项目成员", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/removeXiangMuChengYuan", method = RequestMethod.POST)
    @DtoValid
    public String removeXiangMuChengYuan(@RequestBody RemoveXiangMuChengYuan dto) {
        mainService.removeXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RemoveXiangMuChengYuan {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

    @ApiOperation(value = "导入用户工作记录", tags = {"Admin", "工作记录"})
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

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ImportYongHuGongZuoJiLuDto {
        @NotEmpty
        @Valid
        List<YongHuGongZuoJiLuDto> data;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class YongHuGongZuoJiLuDto {
        @NotBlank
        String yongHuMing;

        @NotBlank
        String xiangMuMingCheng;

        @NotNull
        LocalDateTime kaiShi;

        @NotNull
        LocalDateTime jieShu;

        @NotBlank
        String beiZhu;
    }

    @ApiOperation(value = "删除工作记录", tags = {"Admin", "工作记录"})
    @RequestMapping(value = "/admin/deleteYongHuGongZuoJiLu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteYongHuGongZuoJiLu(@PathVariable Long id) {
        mainService.deleteGongZuoJiLu(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "新建支付", tags = {"Admin", "支付"})
    @RequestMapping(value = "/admin/createZhiFu", method = RequestMethod.POST)
    @DtoValid
    public String createZhiFu(@RequestBody CreateZhiFuDto dto) {
        ZhiFu zhiFu = mainService.createZhiFu(dto.gongSiMingCheng, dto.riQi, dto.jinE, dto.beiZhu);

        return ppResponse.response(zhiFu);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CreateZhiFuDto {
        @NotBlank
        String gongSiMingCheng;

        @NotNull
        LocalDate riQi;

        @NotNull
        BigDecimal jinE;

        String beiZhu;
    }

    @ApiOperation(value = "删除支付", tags = {"Admin", "支付"})
    @RequestMapping(value = "/admin/deleteZhiFu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteZhiFu(@PathVariable Long id) {
        mainService.deleteZhiFu(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "生成报告", notes = "成功生成报告后, 把对应公司的结算日设置为报告结束日期", tags = {"Admin", "报告"})
    @RequestMapping(value = "/admin/generateBaoGao", method = RequestMethod.POST)
    @DtoValid
    public String generateBaoGao(@RequestBody GenerateBaoGaoDto dto) {
        JSONObject report;

        try {
            report = mainService.generateBaoGao(dto.gongSiId, dto.kaiShi, dto.jieShu);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        // -成功生成报告后, 对应公司的结算日如小于报告结束日期, 则设置结算日为报告结束日期
        GongSi gongSi = mainService.gainEntityWithExistsChecking(GongSi.class, dto.gongSiId);
        if (gongSi.getJieSuanRi().isBefore(dto.jieShu)) {
            gongSi.setJieSuanRi(dto.jieShu);
        }
        // -

        return ppResponse.response(report);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class GenerateBaoGaoDto {
        @NotNull
        Long gongSiId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        LocalDate jieShu;
    }
    // -

    @ApiOperation(value = "设置当前用户密码", tags = {"用户"})
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    @DtoValid
    public String changePassword(Authentication authentication, @RequestBody PasswordDto dto) {
        Long yongHuId = ((YongHu) authentication.getPrincipal()).getId();

        mainService.changePassword(yongHuId, dto.password);

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class PasswordDto {
        @NotBlank
        @Size(min = 4)
        String password;
    }

    @ApiOperation(value = "导入本人工作记录", tags = {"用户"})
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

        return ppResponse.response("ok");
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ImportGongZuoJiLuDto {
        @NotEmpty
        @Valid
        List<GongZuoJiLuDto> data;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class GongZuoJiLuDto {
        @NotBlank
        String xiangMuMingCheng;

        @NotNull
        LocalDateTime kaiShi;

        @NotNull
        LocalDateTime jieShu;

        @NotBlank
        String beiZhu;
    }

    @ApiOperation(value = "删除本人工作记录", tags = {"用户"})
    @RequestMapping(value = "/deleteGongZuoJiLu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteGongZuoJiLu(Authentication authentication, @PathVariable Long id) {
        GongZuoJiLu gongZuoJiLu = mainService.gainEntityWithExistsChecking(GongZuoJiLu.class, id);
        String yongHuMing = ((YongHu) authentication.getPrincipal()).getYongHuMing();

        if (!(gongZuoJiLu.getYongHu().getYongHuMing().equals(yongHuMing))) {
            throw new PPBusinessException("只能删除本人的工作记录!");
        }

        mainService.deleteGongZuoJiLu(id);

        return ppResponse.response("ok");
    }

    @ApiOperation(value = "查询自己的工作记录", tags = {"用户"})
    @RequestMapping(value = "/queryGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public String queryGongZuoJiLu(Authentication authentication, @RequestBody QueryGongZuoJiLuDto dto) {
        Long yongHuId = ((YongHu) authentication.getPrincipal()).getId();

        if (dto.kaiShi == null) {
            dto.kaiShi = MIN_DATE;
        }

        if (dto.jieShu == null) {
            dto.jieShu = MAX_DATE;
        }

        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        List<GongZuoJiLu> gongZuoJiLus = mainService.queryGongZuoJiLu(yongHuId, dto.kaiShi.atStartOfDay(), dto.jieShu.plusDays(1).atStartOfDay(), dto.size, dto.page);

        return ppResponse.response(gongZuoJiLus);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class QueryGongZuoJiLuDto {
        LocalDate kaiShi;

        LocalDate jieShu;

        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }
}

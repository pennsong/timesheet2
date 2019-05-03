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
import com.example.timesheet.util.*;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // todo 测试案例
    @ApiOperation(value = "查询用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/queryYongHu", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryYongHuRDto queryYongHu(@RequestBody AdminQueryYongHuDto dto) {
        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        AdminQueryYongHuRDto rDto = new AdminQueryYongHuRDto();

        // code
        rDto.code = "1";

        // data
        Page<YongHu> result = mainService.queryYongHu(dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    AdminQueryYongHuRDto.Item item = new AdminQueryYongHuRDto.Item();
                    item.id = record.getId();
                    item.yongHuMing = record.getYongHuMing();
                    item.xiaoShiFeiYong = record.getXiaoShiFeiYong();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    public static class AdminQueryYongHuDto {
        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class AdminQueryYongHuRDto {
        @Data
        static class Item {
            Long id;
            String yongHuMing;
            BigDecimal xiaoShiFeiYong;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    @ApiOperation(value = "新建用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/createYongHu", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<CreateYongHuRDto> createYongHu(@RequestBody CreateYongHuDto dto) {
        YongHu yongHu = mainService.createYongHu(dto.yongHuMing, dto.miMa, dto.xiaoShiFeiYong);

        CreateYongHuRDto rDto = new CreateYongHuRDto();
        rDto.setId(yongHu.getId());
        rDto.setYongHuMing(yongHu.getYongHuMing());
        rDto.setXiaoShiFeiYong(yongHu.getXiaoShiFeiYong());

        return new PPOKRecord<CreateYongHuRDto>(rDto);
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

    @ApiModel(description = "新建用户RDto")
    @Data
    public static class CreateYongHuRDto {
        @ApiModelProperty(notes = "id", required = true, position = 1)
        Long id;

        @ApiModelProperty(notes = "用户名", required = true, position = 2)
        String yongHuMing;

        @ApiModelProperty(notes = "小时费用", required = true, position = 3)
        BigDecimal xiaoShiFeiYong;
    }

    @ApiOperation(value = "删除用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/deleteYongHu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public PPOK deleteYongHu(@PathVariable Long id) {
        mainService.deleteYongHu(id);

        return PPOK.OK;
    }

    @ApiOperation(value = "设置指定用户密码", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/setYongHuPassword", method = RequestMethod.POST)
    @DtoValid
    public PPOK setYongHuPassword(@RequestBody YongHuPasswordDto dto) {
        mainService.changePassword(dto.yongHuId, dto.password);

        return PPOK.OK;
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

    // todo 测试案例
    @ApiOperation(value = "查询公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/queryGongSi", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryGongSiRDto queryGongSi(@RequestBody AdminQueryGongSiDto dto) {
        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        AdminQueryGongSiRDto rDto = new AdminQueryGongSiRDto();

        // code
        rDto.code = "1";

        // data
        Page<GongSi> result = mainService.queryGongSi(dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    AdminQueryGongSiRDto.Item item = new AdminQueryGongSiRDto.Item();
                    item.id = record.getId();
                    item.mingCheng = record.getMingCheng();
                    item.jieSuanRi = record.getJieSuanRi();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    public static class AdminQueryGongSiDto {
        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class AdminQueryGongSiRDto {
        @Data
        static class Item {
            Long id;
            String mingCheng;
            LocalDate jieSuanRi;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    @ApiOperation(value = "新建公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/createGongSi", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<CreateGongSiRDto> createGongSi(@RequestBody CreateGongSiDto dto) {
        GongSi gongSi = mainService.createGongSi(dto.mingCheng);

        CreateGongSiRDto rDto = new CreateGongSiRDto();
        rDto.setId(gongSi.getId());
        rDto.setMingCheng(gongSi.getMingCheng());

        PPOKRecord<CreateGongSiRDto> r = new PPOKRecord<CreateGongSiRDto>(rDto);

        return r;
    }

    @Data
    public static class CreateGongSiDto {
        @NotBlank
        String mingCheng;
    }

    @Data
    public static class CreateGongSiRDto {
        Long id;
        String mingCheng;
    }

    @ApiOperation(value = "删除公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/deleteGongSi/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public PPOK deleteGongSi(@PathVariable Long id) {
        mainService.deleteGongSi(id);

        return PPOK.OK;
    }

    @ApiOperation(value = "设置公司名称", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/setGongSiMingCheng", method = RequestMethod.POST)
    @DtoValid
    public PPOK setGongSiMingCheng(@RequestBody SetGongSiMingChengDto dto) {
        mainService.setGongSiMingCheng(dto.id, dto.mingCheng);

        return PPOK.OK;
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
    public PPOK setGongSiJieSuanRi(@RequestBody SetGongSiJieSuanRiDto dto) {
        mainService.setGongSiJieSuanRi(dto.id, dto.jieSuanRi);

        return PPOK.OK;
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

    // todo 测试案例
    @ApiOperation(value = "查询项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/queryXiangMu", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryXiangMuRDto queryXiangMu(@RequestBody AdminQueryXiangMuDto dto) {
        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        AdminQueryXiangMuRDto rDto = new AdminQueryXiangMuRDto();

        // code
        rDto.code = "1";

        // data
        Page<XiangMu> result = mainService.queryXiangMu(dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    AdminQueryXiangMuRDto.Item item = new AdminQueryXiangMuRDto.Item();
                    item.id = record.getId();
                    item.mingCheng = record.getMingCheng();
                    item.gongSiObjMingCheng = record.getGongSi().getMingCheng();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    public static class AdminQueryXiangMuDto {
        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class AdminQueryXiangMuRDto {
        @Data
        static class Item {
            Long id;
            String mingCheng;
            String gongSiObjMingCheng;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    // todo 测试案例
    @ApiOperation(value = "查询单个项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/queryXiangMu/{id}", method = RequestMethod.GET)
    @DtoValid
    public PPOKRecord<AdminQueryDanGeXiangMuRDto> queryDanGeXiangMu(@PathVariable Long id) {
        XiangMu xiangMu = mainService.gainEntityWithExistsChecking(XiangMu.class, id);

        AdminQueryDanGeXiangMuRDto rDto = new AdminQueryDanGeXiangMuRDto();
        rDto.id = xiangMu.getId();
        rDto.mingCheng = xiangMu.getMingCheng();
        rDto.jiFeiBiaoZhunRDtos = xiangMu.getJiFeiBiaoZhuns().stream().map(item -> {
            AdminQueryDanGeXiangMuRDto.JiFeiBiaoZhunRDto jiFeiBiaoZhunRDto = new AdminQueryDanGeXiangMuRDto.JiFeiBiaoZhunRDto();
            jiFeiBiaoZhunRDto.yongHuObjId = item.getYongHu().getId();
            jiFeiBiaoZhunRDto.yongHuObjYongHuMing = item.getYongHu().getYongHuMing();
            jiFeiBiaoZhunRDto.kaiShi = item.getKaiShi();
            jiFeiBiaoZhunRDto.xiaoShiFeiYong = item.getXiaoShiFeiYong();

            return jiFeiBiaoZhunRDto;
        }).collect(Collectors.toList());

        return new PPOKRecord<>(rDto);
    }

    @Data
    static class AdminQueryDanGeXiangMuRDto {
        @Data
        static class JiFeiBiaoZhunRDto {
            Long yongHuObjId;
            String yongHuObjYongHuMing;
            LocalDate kaiShi;
            BigDecimal xiaoShiFeiYong;
        }

        Long id;
        String mingCheng;
        List<JiFeiBiaoZhunRDto> jiFeiBiaoZhunRDtos;
    }

    @ApiOperation(value = "新建项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/createXiangMu", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<createXiangMuRDto> createXiangMu(@RequestBody createXiangMuDto dto) {
        XiangMu xiangMu = mainService.createXiangMu(dto.mingCheng, dto.gongSiId);

        createXiangMuRDto rDto = new createXiangMuRDto();
        rDto.id = xiangMu.getId();
        rDto.gongSiObjId = xiangMu.getGongSi().getId();
        rDto.mingCheng = xiangMu.getMingCheng();

        return new PPOKRecord(rDto);
    }

    @Data
    public static class createXiangMuDto {
        @NotBlank
        String mingCheng;

        @NotNull
        Long gongSiId;
    }

    @Data
    public static class createXiangMuRDto {
        Long id;
        Long gongSiObjId;
        String mingCheng;
    }

    @ApiOperation(value = "设置项目名称", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/setXiangMuMingCheng", method = RequestMethod.POST)
    @DtoValid
    public PPOK setXiangMuMingCheng(@RequestBody SetXiangMuMingCheng dto) {
        mainService.setXiangMuMingCheng(dto.id, dto.mingCheng);

        return PPOK.OK;
    }

    @Data
    public static class SetXiangMuMingCheng {
        @NotNull
        Long id;

        @NotBlank
        String mingCheng;
    }

    @ApiOperation(value = "删除项目", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/deleteXiangMu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public PPOK deleteXiangMu(@PathVariable Long id) {
        mainService.deleteXiangMu(id);

        return PPOK.OK;
    }

    @ApiOperation(value = "添加项目计费标准", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/addXiangMuJiFeiBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public PPOK addXiangMuJiFeiBiaoZhun(@RequestBody AddXiangMuJiFeiBiaoZhunDto dto) {
        mainService.addXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi, dto.xiaoShiFeiYong);

        return PPOK.OK;
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
    public PPOK removeXiangMuJiFeiBiaoZhun(@RequestBody RemoveXiangMuJiFeiBiaoZhun dto) {
        mainService.removeXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi);

        return PPOK.OK;
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
    public PPOK addXiangMuChengYuan(@RequestBody AddXiangMuChengYuan dto) {
        mainService.addXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPOK.OK;
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
    public PPOK removeXiangMuChengYuan(@RequestBody RemoveXiangMuChengYuan dto) {
        mainService.removeXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPOK.OK;
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
    public PPOK importYongHuGongZuoJiLu(@RequestBody ImportYongHuGongZuoJiLuDto dto) {
        for (YongHuGongZuoJiLuDto item : dto.data) {
            mainService.createGongZuoJiLu(item.yongHuMing,
                    item.xiangMuMingCheng,
                    item.kaiShi,
                    item.jieShu,
                    item.beiZhu);
        }

        return PPOK.OK;
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
    public PPOK deleteYongHuGongZuoJiLu(@PathVariable Long id) {
        mainService.deleteGongZuoJiLu(id);

        return PPOK.OK;
    }

    @ApiOperation(value = "新建支付", tags = {"Admin", "支付"})
    @RequestMapping(value = "/admin/createZhiFu", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<CreateZhiFuRDto> createZhiFu(@RequestBody CreateZhiFuDto dto) {
        ZhiFu zhiFu = mainService.createZhiFu(dto.gongSiMingCheng, dto.riQi, dto.jinE, dto.beiZhu);

        CreateZhiFuRDto rDto = new CreateZhiFuRDto();
        rDto.id = zhiFu.getId();
        rDto.gongSiObjMingCheng = zhiFu.getGongSi().getMingCheng();
        rDto.riQi = zhiFu.getRiQi();
        rDto.jinE = zhiFu.getJingE();
        rDto.beiZhu = zhiFu.getBeiZhu();

        return new PPOKRecord<>(rDto);
    }

    @Data
    static class CreateZhiFuDto {
        @NotBlank
        String gongSiMingCheng;

        @NotNull
        LocalDate riQi;

        @NotNull
        BigDecimal jinE;

        String beiZhu;
    }

    @Data
    static class CreateZhiFuRDto {
        Long id;
        String gongSiObjMingCheng;
        LocalDate riQi;
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

    // todo 测试案例
    @ApiOperation(value = "查询工作记录", tags = {"Admin", "支付"})
    @RequestMapping(value = "/admin/queryGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryGongZuoJiLuRDto queryGongZuoJiLu(@RequestBody AdminQueryGongZuoJiLuDto dto) {
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

        AdminQueryGongZuoJiLuRDto rDto = new AdminQueryGongZuoJiLuRDto();

        // code
        rDto.code = "1";

        // data
        Page<GongZuoJiLu> result = mainService.queryGongZuoJiLu(dto.yongHuId, dto.gongSiId, dto.kaiShi.atStartOfDay(), dto.jieShu.plusDays(1).atStartOfDay(), dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    AdminQueryGongZuoJiLuRDto.Item item = new AdminQueryGongZuoJiLuRDto.Item();
                    item.id = record.getId();
                    item.kaiShi = record.getKaiShi();
                    item.jieShu = record.getJieShu();
                    item.gongSiObjMingCheng = record.getYongHu().getYongHuMing();
                    item.xiangMuObjMingCheng = record.getXiangMu().getMingCheng();
                    item.yongHuObjYongHuMing = record.getYongHu().getYongHuMing();
                    item.beiZhu = record.getBeiZhu();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    static class AdminQueryGongZuoJiLuDto {
        Long gongSiId;

        Long yongHuId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate kaiShi;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate jieShu;

        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class AdminQueryGongZuoJiLuRDto {
        @Data
        static class Item {
            Long id;
            LocalDateTime kaiShi;
            LocalDateTime jieShu;
            String gongSiObjMingCheng;
            String xiangMuObjMingCheng;
            String yongHuObjYongHuMing;
            String beiZhu;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
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
    public PPOK changePassword(Authentication authentication, @RequestBody PasswordDto dto) {
        Long yongHuId = ((PPJson) (authentication.getPrincipal())).getLong("yongHuId");

        mainService.changePassword(yongHuId, dto.password);

        return PPOK.OK;
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
    public PPOK importGongZuoJiLu(Authentication authentication, @RequestBody ImportGongZuoJiLuDto dto) {
        for (GongZuoJiLuDto item : dto.data) {
            String yongHuMing = ((PPJson) (authentication.getPrincipal())).getString("yongHuMing");

            mainService.createGongZuoJiLu(yongHuMing,
                    item.xiangMuMingCheng,
                    item.kaiShi,
                    item.jieShu,
                    item.beiZhu);
        }

        return PPOK.OK;
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
    public PPOK deleteGongZuoJiLu(Authentication authentication, @PathVariable Long id) {
        GongZuoJiLu gongZuoJiLu = mainService.gainEntityWithExistsChecking(GongZuoJiLu.class, id);
        String yongHuMing = ((PPJson) (authentication.getPrincipal())).getString("yongHuMing");

        if (!(gongZuoJiLu.getYongHu().getYongHuMing().equals(yongHuMing))) {
            throw new PPBusinessException("只能删除本人的工作记录!");
        }

        mainService.deleteGongZuoJiLu(id);

        return PPOK.OK;
    }

    @ApiOperation(value = "查询自己的工作记录", tags = {"用户"})
    @RequestMapping(value = "/queryGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public QueryGongZuoJiLuRDto queryGongZuoJiLu(Authentication authentication, @RequestBody QueryGongZuoJiLuDto dto) {
        Long yongHuId = ((PPJson) (authentication.getPrincipal())).getLong("yongHuId");

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

        QueryGongZuoJiLuRDto rDto = new QueryGongZuoJiLuRDto();

        // code
        rDto.code = "1";

        // data
        Page<GongZuoJiLu> result = mainService.queryGongZuoJiLu(yongHuId, dto.gongSiId, dto.kaiShi.atStartOfDay(), dto.jieShu.plusDays(1).atStartOfDay(), dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    QueryGongZuoJiLuRDto.Item item = new QueryGongZuoJiLuRDto.Item();
                    item.id = record.getId();
                    item.kaiShi = record.getKaiShi();
                    item.jieShu = record.getJieShu();
                    item.gongSiObjMngCheng = record.getYongHu().getYongHuMing();
                    item.xiangMuObjMingCheng = record.getXiangMu().getMingCheng();
                    item.yongHuObjYongHuMing = record.getYongHu().getYongHuMing();
                    item.beiZhu = record.getBeiZhu();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    static class QueryGongZuoJiLuDto {
        Long gongSiId;

        Long yongHuId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate kaiShi;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate jieShu;

        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class QueryGongZuoJiLuRDto {
        @Data
        static class Item {
            Long id;
            LocalDateTime kaiShi;
            LocalDateTime jieShu;
            String gongSiObjMngCheng;
            String xiangMuObjMingCheng;
            String yongHuObjYongHuMing;
            String beiZhu;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }
}

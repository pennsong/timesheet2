package com.example.timesheet.controller;

import com.example.timesheet.aop.DtoValid;
import com.example.timesheet.exception.PPBusinessException;
import com.example.timesheet.exception.PPValidateException;
import com.example.timesheet.model.*;
import com.example.timesheet.service.MainService;
import com.example.timesheet.service.PPResponse;
import com.example.timesheet.util.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONException;
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
import java.time.format.DateTimeFormatter;
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

    // -Admin

    // todo 测试案例
    @ApiOperation(value = "查询用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/queryYongHu", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryYongHuRDto queryYongHu(@RequestBody PaginationDto dto) {
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
                    item.xiaoShiTiCheng = record.getXiaoShiTiCheng();
                    item.jieSuanRi = record.getJieSuanRi();

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
            BigDecimal xiaoShiTiCheng;
            LocalDate jieSuanRi;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    @ApiOperation(value = "新建用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/createYongHu", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<CreateYongHuRDto> createYongHu(@RequestBody CreateYongHuDto dto) {
        YongHu yongHu = mainService.createYongHu(dto.yongHuMing, dto.miMa, dto.xiaoShiFeiYong, dto.xiaoShiTiCheng);

        CreateYongHuRDto rDto = new CreateYongHuRDto();
        rDto.setId(yongHu.getId());
        rDto.setYongHuMing(yongHu.getYongHuMing());
        rDto.setXiaoShiFeiYong(yongHu.getXiaoShiFeiYong());
        rDto.setXiaoShiTiCheng(yongHu.getXiaoShiTiCheng());

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
        
        @ApiModelProperty(notes = "小时提成", required = true, position = 4)
        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal xiaoShiTiCheng;
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
        
        @ApiModelProperty(notes = "小时提成", required = true, position = 4)
        BigDecimal xiaoShiTiCheng;
    }

    @ApiOperation(value = "删除用户", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/deleteYongHu/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public PPOK deleteYongHu(@PathVariable Long id) {
        mainService.deleteYongHu(id);

        return PPOK.OK;
    }
    
    @ApiOperation(value = "设置用户个人结算日", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/setYongHuJieSuanRi", method = RequestMethod.POST)
    @DtoValid
    public PPOK setYongHuJieSuanRi(@RequestBody SetYongHuJieSuanRiDto dto) {
        mainService.setYongHuJieSuanRi(dto.id, dto.jieSuanRi);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class SetYongHuJieSuanRiDto {
        @NotNull
        Long id;

        @NotNull
        LocalDate jieSuanRi;
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

    @ApiOperation(value = "更新用户费用标准", tags = {"Admin", "用户"})
    @RequestMapping(value = "/admin/setYongHuFeiYongBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public PPOK setYongHuFeiYongBiaoZhun(@RequestBody UpdateYongHuFeiYongBiaoZhunDto dto) {
        mainService.changeYongHuFeiYongBiaoZhun(dto.yongHuId, dto.xiaoShiFeiYong, dto.xiaoShiTiCheng);
        return PPOK.OK;
    }

    @ApiModel(description = "更新用户费用标准Dto")
    @Data
    public static class UpdateYongHuFeiYongBiaoZhunDto {
        @NotNull
        Long yongHuId;

        @ApiModelProperty(notes = "小时费用", required = true)
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal xiaoShiFeiYong;

        @ApiModelProperty(notes = "小时提成", required = true)
        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal xiaoShiTiCheng;
    }


    // todo 测试案例
    @ApiOperation(value = "查询公司", tags = {"Admin", "公司"})
    @RequestMapping(value = "/admin/queryGongSi", method = RequestMethod.POST)
    @DtoValid
    public AdminQueryGongSiRDto queryGongSi(@RequestBody PaginationDto dto) {
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
    public static class PaginationDto {
    	    @Min(0)
    	    @Max(200)
    	    Integer size;
    	    
    	    @Min(0)
    	    Integer page;
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
        rDto.setJieSuanRi(gongSi.getJieSuanRi());

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
        LocalDate jieSuanRi;
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
    public AdminQueryXiangMuRDto queryXiangMu(@RequestBody PaginationDto dto) {
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
        rDto.tiChengBiaoZhunRDtos = xiangMu.getTiChengBiaoZhuns().stream().map(item -> {
            AdminQueryDanGeXiangMuRDto.TiChengBiaoZhunRDto tiChengBiaoZhunRDto = new AdminQueryDanGeXiangMuRDto.TiChengBiaoZhunRDto();
            tiChengBiaoZhunRDto.yongHuObjId = item.getYongHu().getId();
            tiChengBiaoZhunRDto.yongHuObjYongHuMing = item.getYongHu().getYongHuMing();
            tiChengBiaoZhunRDto.kaiShi = item.getKaiShi();
            tiChengBiaoZhunRDto.xiaoShiTiCheng = item.getXiaoShiTiCheng();
            return tiChengBiaoZhunRDto;
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
        @Data
        static class TiChengBiaoZhunRDto {
            Long yongHuObjId;
            String yongHuObjYongHuMing;
            LocalDate kaiShi;
            BigDecimal xiaoShiTiCheng;
        }

        Long id;
        String mingCheng;
        List<JiFeiBiaoZhunRDto> jiFeiBiaoZhunRDtos;
        List<TiChengBiaoZhunRDto> tiChengBiaoZhunRDtos;
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
    public PPOK setXiangMuMingCheng(@RequestBody SetXiangMuMingChengDto dto) {
        mainService.setXiangMuMingCheng(dto.id, dto.mingCheng);

        return PPOK.OK;
    }

    @Data
    public static class SetXiangMuMingChengDto {
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
    public PPOK removeXiangMuJiFeiBiaoZhun(@RequestBody RemoveXiangMuJiFeiBiaoZhunDto dto) {
        mainService.removeXiangMuJiFeiBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RemoveXiangMuJiFeiBiaoZhunDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;
    }
    
    @ApiOperation(value = "添加项目提成标准", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/addXiangMuTiChengBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public PPOK addXiangMuTiChengBiaoZhun(@RequestBody AddXiangMuTiChengBiaoZhunDto dto) {
        mainService.addXiangMuTiChengBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi, dto.xiaoShiTiCheng);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AddXiangMuTiChengBiaoZhunDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        BigDecimal xiaoShiTiCheng;
    }

    @ApiOperation(value = "移除项目提成标准", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/removeXiangMuTiChengBiaoZhun", method = RequestMethod.POST)
    @DtoValid
    public PPOK removeXiangMuTiChengBiaoZhun(@RequestBody RemoveXiangMuTiChengBiaoZhunDto dto) {
        mainService.removeXiangMuTiChengBiaoZhun(dto.xiangMuId, dto.yongHuId, dto.kaiShi);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RemoveXiangMuTiChengBiaoZhunDto {
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
    public PPOK addXiangMuChengYuan(@RequestBody AddXiangMuChengYuanDto dto) {
        mainService.addXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AddXiangMuChengYuanDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

    @ApiOperation(value = "移除项目成员", tags = {"Admin", "项目"})
    @RequestMapping(value = "/admin/removeXiangMuChengYuan", method = RequestMethod.POST)
    @DtoValid
    public PPOK removeXiangMuChengYuan(@RequestBody RemoveXiangMuChengYuanDto dto) {
        mainService.removeXiangMuChengYuan(dto.xiangMuId, dto.yongHuId);

        return PPOK.OK;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class RemoveXiangMuChengYuanDto {
        @NotNull
        Long xiangMuId;

        @NotNull
        Long yongHuId;
    }

 // todo 测试案例
    @ApiOperation(value = "查询工作记录", tags = {"Admin", "工作记录"})
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
                    item.gongSiObjMingCheng = record.getXiangMu().getGongSi().getMingCheng();
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
    
    @ApiOperation(value = "导入用户工作记录", tags = {"Admin", "工作记录"})
    @RequestMapping(value = "/admin/importYongHuGongZuoJiLu", method = RequestMethod.POST)
    @DtoValid
    public PPOK importYongHuGongZuoJiLu(@RequestBody ImportYongHuGongZuoJiLuDto dto) {
    		// 因最小计算单位为秒，因此先行去除时间的微秒数	
   		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (YongHuGongZuoJiLuDto item : dto.data) {
        		LocalDateTime kaiShi = LocalDateTime.parse(item.kaiShi.format(formatter));
            LocalDateTime jieShu = LocalDateTime.parse(item.jieShu.format(formatter));
            if(kaiShi.isEqual(jieShu) || kaiShi.isAfter(jieShu)) {
	    	    		throw new PPBusinessException("工作记录的开始时间等于或晚于结束时间，不允许添加！");
            }
            mainService.createGongZuoJiLu(item.yongHuMing,
                    item.xiangMuMingCheng,
                    kaiShi,
                    jieShu,
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

    // todo 测试案例
    @ApiOperation(value = "查询支付", tags = {"Admin", "支付"})
    @RequestMapping(value = "/admin/queryZhiFu", method = RequestMethod.POST)
    @DtoValid
    public QueryZhiFuRDto queryZhiFu(@RequestBody PaginationDto dto) {
        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        QueryZhiFuRDto rDto = new QueryZhiFuRDto();

        // code
        rDto.code = "1";

        // data
        Page<ZhiFu> result = mainService.queryZhiFu(dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    QueryZhiFuRDto.Item item = new QueryZhiFuRDto.Item();
                    item.id = record.getId();
                    item.gongSiObjMingCheng = record.getGongSi().getMingCheng();
                    item.riQi = record.getRiQi();
                    item.jinE = record.getJinE();
                    item.beiZhu = record.getBeiZhu();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    public static class QueryZhiFuDto {
        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class QueryZhiFuRDto {
        @Data
        static class Item {
            Long id;
            String gongSiObjMingCheng;
            LocalDate riQi;
            BigDecimal jinE;
            String beiZhu;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
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
        rDto.jinE = zhiFu.getJinE();
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
    
    @ApiOperation(value = "查询提成", tags = {"Admin", "提成"})
    @RequestMapping(value = "/admin/queryTiCheng", method = RequestMethod.POST)
    @DtoValid
    public QueryTiChengRDto queryTiCheng(@RequestBody PaginationDto dto) {
        if (dto.size == null) {
            dto.size = 50;
        }

        if (dto.page == null) {
            dto.page = 0;
        }

        QueryTiChengRDto rDto = new QueryTiChengRDto();

        // code
        rDto.code = "1";

        // data
        Page<TiCheng> result = mainService.queryTiCheng(dto.size, dto.page);

        rDto.setData(
                result.getContent().stream().map(record -> {
                    QueryTiChengRDto.Item item = new QueryTiChengRDto.Item();
                    item.id = record.getId();
                    item.yongHuObjMing = record.getYongHu().getYongHuMing();
                    item.riQi = record.getRiQi();
                    item.jinE = record.getJinE();
                    item.beiZhu = record.getBeiZhu();

                    return item;
                }).collect(Collectors.toList())
        );

        // ppPageInfo
        rDto.setPpPageInfo(PPUtil.getPPPageInfo(result));

        return rDto;
    }

    @Data
    public static class QueryTiChengDto {
        @Min(0)
        @Max(200)
        Integer size;

        @Min(0)
        Integer page;
    }

    @Data
    static class QueryTiChengRDto {
        @Data
        static class Item {
            Long id;
            String yongHuObjMing;
            LocalDate riQi;
            BigDecimal jinE;
            String beiZhu;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    @ApiOperation(value = "新建提成", tags = {"Admin", "提成"})
    @RequestMapping(value = "/admin/createTiCheng", method = RequestMethod.POST)
    @DtoValid
    public PPOKRecord<CreateTiChengRDto> createTiCheng(@RequestBody CreateTiChengDto dto) {
        TiCheng tiCheng = mainService.createTiCheng(dto.yongHuMing, dto.riQi, dto.jinE, dto.beiZhu);

        CreateTiChengRDto rDto = new CreateTiChengRDto();
        rDto.id = tiCheng.getId();
        rDto.yongHuObjMing = tiCheng.getYongHu().getYongHuMing();
        rDto.riQi = tiCheng.getRiQi();
        rDto.jinE = tiCheng.getJinE();
        rDto.beiZhu = tiCheng.getBeiZhu();

        return new PPOKRecord<>(rDto);
    }

    @Data
    static class CreateTiChengDto {
        @NotBlank
        String yongHuMing;

        @NotNull
        LocalDate riQi;

        @NotNull
        BigDecimal jinE;

        String beiZhu;
    }

    @Data
    static class CreateTiChengRDto {
        Long id;
        String yongHuObjMing;
        LocalDate riQi;
        BigDecimal jinE;
        String beiZhu;
    }

    @ApiOperation(value = "删除提成", tags = {"Admin", "提成"})
    @RequestMapping(value = "/admin/deleteTiCheng/{id}", method = RequestMethod.DELETE)
    @DtoValid
    public String deleteTiCheng(@PathVariable Long id) {
        mainService.deleteTiCheng(id);

        return ppResponse.response("ok");
    }


    @ApiOperation(value = "生成报告", notes = "成功生成报告后, 把对应公司的结算日设置为报告结束日期。其中开始日期不可以大于结束日期", tags = {"Admin", "报告"})
    @RequestMapping(value = "/admin/generateBaoGao", method = RequestMethod.POST)
    @DtoValid
    public String generateBaoGao(@RequestBody GenerateBaoGaoDto dto) {
    		if(dto.kaiShi.isAfter(dto.jieShu)) {
			throw new PPBusinessException("开始日期不可以大于结束日期");
		}
        JSONObject report;
        try {
            report = mainService.generateBaoGao(dto.gongSiId, dto.kaiShi, dto.jieShu);
        } catch(JSONException je) {
        	    throw new PPValidateException(je.getMessage());
        }
        if (dto.setJieSuanRi == true) {
            // -成功生成报告后, 对应公司的结算日如小于报告结束日期, 则设置结算日为报告结束日期
            GongSi gongSi = mainService.gainEntityWithExistsChecking(GongSi.class, dto.gongSiId);
            if (gongSi.getJieSuanRi().isBefore(dto.jieShu)) {
                gongSi.setJieSuanRi(dto.jieShu);
            }
            // -
        }

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

        @NotNull
        Boolean setJieSuanRi;
    }
    
    @ApiOperation(value = "生成用户报告", notes = "成功生成用户报告后, 把对应用户的结算日设置为报告结束日期。其中开始日期不可以大于结束日期", tags = {"Admin", "报告"})
    @RequestMapping(value = "/admin/generateYongHuBaoGao", method = RequestMethod.POST)
    @DtoValid
    public String generateYongHuBaoGao(@RequestBody GenerateYongHuBaoGaoDto dto) {
    		if(dto.kaiShi.isAfter(dto.jieShu)) {
			throw new PPBusinessException("开始日期不可以大于结束日期");
		}
    		JSONObject report;
        	try {
        		report = mainService.generateYongHuBaoGao(dto.yongHuId, dto.kaiShi, dto.jieShu);
        	} catch(JSONException je) {
        		throw new PPValidateException(je.getMessage());
        	}
        if (dto.setJieSuanRi == true) {
            // -成功生成报告后, 对应用户的结算日如小于报告结束日期, 则设置结算日为报告结束日期
        	    YongHu yongHu = mainService.gainEntityWithExistsChecking(YongHu.class, dto.yongHuId);
            if (yongHu.getJieSuanRi().isBefore(dto.jieShu)) {
                yongHu.setJieSuanRi(dto.jieShu);
            }
            // -
        }

        return ppResponse.response(report);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class GenerateYongHuBaoGaoDto {
        @NotNull
        Long yongHuId;

        @NotNull
        LocalDate kaiShi;

        @NotNull
        LocalDate jieShu;

        @NotNull
        Boolean setJieSuanRi;
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
        // 因最小计算单位为秒，因此先行去除时间的微秒数	
   		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (GongZuoJiLuDto item : dto.data) {
            String yongHuMing = ((PPJson) (authentication.getPrincipal())).getString("yongHuMing");

            if (!(yongHuMing.equals(item.yongHuMing))) {
                throw new PPBusinessException("只能导入自己的工作记录!");
            }
            LocalDateTime kaiShi = LocalDateTime.parse(item.kaiShi.format(formatter));
            LocalDateTime jieShu = LocalDateTime.parse(item.jieShu.format(formatter));
            if(kaiShi.isEqual(jieShu) || kaiShi.isAfter(jieShu)) {
	    	    		throw new PPBusinessException("工作记录的开始时间等于或晚于结束时间，不允许添加！");
            }
            mainService.createGongZuoJiLu(yongHuMing,
                    item.xiangMuMingCheng,
                    kaiShi,
                    jieShu,
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

        @NotBlank
        String yongHuMing;

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
                    item.gongSiObjMingCheng = record.getXiangMu().getGongSi().getMingCheng();
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
            String gongSiObjMingCheng;
            String xiangMuObjMingCheng;
            String yongHuObjYongHuMing;
            String beiZhu;
        }

        String code;
        List<Item> data;
        PPPageInfo ppPageInfo;
    }

    @ApiOperation(value = "生成本人用户报告", notes = "本人报告仅预览。其中开始日期不可以大于结束日期", tags = {"用户"})
    @RequestMapping(value = "/generateOwnYongHuBaoGao", method = RequestMethod.POST)
    @DtoValid
    public String generateOwnYongHuBaoGao(Authentication authentication, @RequestBody GenerateOwnYongHuBaoGaoDto dto){  
    		if(dto.kaiShi.isAfter(dto.jieShu)) {
			throw new PPBusinessException("开始日期不可以大于结束日期");
		}
    		JSONObject report;

        Long yongHuId = ((PPJson) (authentication.getPrincipal())).getLong("yongHuId");
        try {
			report = mainService.generateYongHuBaoGao(yongHuId, dto.kaiShi, dto.jieShu);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new PPValidateException(e.getMessage());
		}

        return ppResponse.response(report);
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    static class GenerateOwnYongHuBaoGaoDto {
    	    @NotNull
    	    LocalDate kaiShi;
    	    @NotNull
    	    LocalDate jieShu;
    }
}

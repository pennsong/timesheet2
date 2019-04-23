package com.example.timesheet.controller;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RestController
//@RequestMapping(consumes = "application/json", produces = "application/json")
public class HomeController {
    @AllArgsConstructor
    @Data
    class Test {
        @NotNull
        @Size(min = 2)
        String n1;

        @NotNull
        Long n2;
    }

    @RequestMapping(value = "/firstPage")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/auth/test")
    public String auth() {
        return "auth ok";
    }

    @ApiOperation(value = "测试", notes = "abcdef")
    @RequestMapping("/all/test")
    public Test all() {
        return new Test("ab", 1L);
    }

    @RequestMapping("/all/test2")
    public Test all2(TestDto dto) {
        return new Test("ab", 1L);
    }

    @Data
    class TestDto {
        int size;
    }

}
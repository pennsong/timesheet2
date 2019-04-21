package com.example.timesheet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(consumes = "application/json", produces = "application/json")
public class HomeController {
    @RequestMapping(value = "/firstPage")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/auth/test")
    public String auth() {
        return "auth ok";
    }

}
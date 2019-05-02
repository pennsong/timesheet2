package com.example.timesheet.util;

import lombok.Data;

@Data
public class PPOK {
    public static PPOK OK = new PPOK();

    String code = "1";
    String data = "OK";
}

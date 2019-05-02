package com.example.timesheet.util;

import lombok.Data;

@Data
public class PPOKRecord<T> {
    String code = "1";
    T data;

    public PPOKRecord(T data) {
        this.data = data;
    }
}

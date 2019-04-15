package com.example.timesheet.exception;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleBadRequest(HttpServletRequest req, Exception ex) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        if (ex instanceof PPBusinessException) {
            jsonObject.put("code", "1000");
        } else if (ex instanceof PPDeleteReferenceException) {
            jsonObject.put("code", "2000");
        } else if (ex instanceof PPDuplicateException) {
            jsonObject.put("code", "3000");
        } else if (ex instanceof PPValidateException) {
            jsonObject.put("code", "4000");
        } else if (ex instanceof NoSuchElementException || ex instanceof EmptyResultDataAccessException) {
            jsonObject.put("code", "5000");
        } else {
            jsonObject.put("code", "10000");
        }


        jsonObject.put("message", ex.getMessage());

        return jsonObject.toString();
    }
}

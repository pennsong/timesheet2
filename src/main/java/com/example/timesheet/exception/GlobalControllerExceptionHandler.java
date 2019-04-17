package com.example.timesheet.exception;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        String customMessage = null;

        if (ex instanceof PPBusinessException) {
            jsonObject.put("code", "1000");
        } else if (ex instanceof PPDeleteReferenceException) {
            jsonObject.put("code", "2000");
        } else if (ex instanceof PPDuplicateException || ex instanceof DataIntegrityViolationException) {
            if (ex instanceof PPDuplicateException) {
                jsonObject.put("code", "3000");
            } else {
                if (ex.getMessage().contains("constraint") && ex.getMessage().contains("UK")) {
                    jsonObject.put("code", "3000");
                    customMessage = "违反唯一约束!";
                } else if (ex.getMessage().contains("REFERENCES") && ex.getMessage().contains("FOREIGN")) {
                    jsonObject.put("code", "3500");
                    customMessage = "被引用, 不可被删除!";
                } else {
                    jsonObject.put("code", "10000");
                }
            }


        } else if (ex instanceof PPValidateException || ex instanceof HttpMessageNotReadableException) {
            jsonObject.put("code", "4000");
        } else if (ex instanceof NoSuchElementException
                || ex instanceof EmptyResultDataAccessException
                || ex instanceof UsernameNotFoundException
                || ex instanceof PPItemNotExistException) {
            jsonObject.put("code", "5000");
        } else {
            jsonObject.put("code", "10000");
        }

        jsonObject.put("message", customMessage == null ? ex.getMessage() : customMessage);

        return jsonObject.toString();
    }
}

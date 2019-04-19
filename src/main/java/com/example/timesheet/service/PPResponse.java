package com.example.timesheet.service;

import com.example.timesheet.exception.PPValidateException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

@Service
public class PPResponse {
    @Autowired
    private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    public String response(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", "1");
            jsonObject.put("message", message);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        return jsonObject.toString();
    }

    public String response(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", "1");
            jsonObject.put("data", data);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        return jsonObject.toString();
    }

    public String response(Object data) {
        try {
            ObjectMapper objectMapper = springMvcJacksonConverter.getObjectMapper();
            String dataToJson = objectMapper.writeValueAsString(data);

            return "{\"code\": \"1\", \"data\":" + dataToJson + "}";
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }

    public Long gainId(ResponseEntity<String> response) {
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            Long id = jsonObject.getJSONObject("data").getLong("id");

            return id;
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }
}

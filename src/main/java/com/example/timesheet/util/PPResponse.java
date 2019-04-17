package com.example.timesheet.util;

import com.example.timesheet.exception.PPValidateException;
import org.json.JSONException;
import org.json.JSONObject;

public class PPResponse {
    public static String response(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", "1");
            jsonObject.put("message", message);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        return jsonObject.toString();
    }

    public static String response(JSONObject data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", 1);
            jsonObject.put("data", data);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }

        return jsonObject.toString();
    }
}

package com.example.timesheet.util;

import com.example.timesheet.exception.PPValidateException;
import org.json.JSONException;
import org.json.JSONObject;

public class PPJson extends JSONObject {
    @Override
    public JSONObject put(String s, boolean i) {
        try {
            return super.put(s, i);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }

    @Override
    public JSONObject put(String s, double i) {
        try {
            return super.put(s, i);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }

    @Override
    public JSONObject put(String s, int i) {
        try {
            return super.put(s, i);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }

    @Override
    public JSONObject put(String s, long i) {
        try {
            return super.put(s, i);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }

    @Override
    public JSONObject put(String s, Object i) {
        try {
            return super.put(s, i);
        } catch (Exception e) {
            throw new PPValidateException(e.getMessage());
        }
    }
}

package com.hankun.ship.bean.response;

/**
 * Created by nan on 2017/12/12.
 */
public class ResponseData {
    private int code = -1;
    private String message = "";
    private Object data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return this.data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

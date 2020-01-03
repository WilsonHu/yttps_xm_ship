package com.hankun.ship.bean;

import com.hankun.ship.bean.response.ResponseData;

import java.util.ArrayList;

public class DeviceListResponse extends ResponseData {
    private ArrayList<String> data;

    @Override
    public ArrayList<String> getData() {
        return data;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
    }
}

package com.hankun.ship.bean;

import java.util.HashMap;

public class FaceDetectDTO {
    private String imageBase64;
    private HashMap<String, String> attributes;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }
}

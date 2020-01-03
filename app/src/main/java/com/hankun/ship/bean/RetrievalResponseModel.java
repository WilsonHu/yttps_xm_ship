package com.hankun.ship.bean;

import java.util.ArrayList;

public class RetrievalResponseModel {
    private ArrayList<PersonRetrievalResultDTO> result;

    private int rtn;

    private String message;

    private int total;

    public ArrayList<PersonRetrievalResultDTO> getResult() {
        return result;
    }

    public int getRtn() {
        return rtn;
    }

    public String getMessage() {
        return message;
    }

    public void setResult(ArrayList<PersonRetrievalResultDTO> result) {
        this.result = result;
    }

    public void setRtn(int rtn) {
        this.rtn = rtn;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

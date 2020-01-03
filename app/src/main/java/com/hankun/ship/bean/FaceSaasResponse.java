/**
  * Copyright 2019 bejson.com 
  */
package com.hankun.ship.bean;

/**
 * Auto-generated: 2019-10-24 13:3:1
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class FaceSaasResponse {

    private int rtn;
    private String message;
    private String global_request_id;
    private CheckImageResult check_image_result;
    public void setRtn(int rtn) {
         this.rtn = rtn;
     }
     public int getRtn() {
         return rtn;
     }

    public void setMessage(String message) {
         this.message = message;
     }
     public String getMessage() {
         return message;
     }

    public void setGlobal_request_id(String global_request_id) {
         this.global_request_id = global_request_id;
     }
     public String getGlobal_request_id() {
         return global_request_id;
     }

    public void setCheck_image_result(CheckImageResult check_image_result) {
         this.check_image_result = check_image_result;
     }
     public CheckImageResult getCheck_image_result() {
         return check_image_result;
     }




}
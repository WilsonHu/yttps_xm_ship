package com.hankun.ship.app;

/**
 * Created by HT on 12/15/2019.
 */
public class URL {
//    public static final String BASE_URL = "http://10.250.62.230:9090/";
    public static final String BASE_URL = "https://fr-api-sit-cruise.dreamcruiselinedev.com:9090/";
//    public static final String BASE_URL = "https://10.250.62.49:9090/";
//    public static final String PARK_URL = "https://211.144.105.121:9812";
//    public static final String LOGIN = "/user/login";
    public static final String LOGOUT = BASE_URL + "/device/account/logout/";
    public static final String USER_LOGIN = BASE_URL + "device/account/login";
    public static final String FACE_DETECT = BASE_URL + "middle/atom/image/recognize";
    public static final String DEVICE_URL = BASE_URL + "device/account/search";
    public static final String CRUISE_URL = BASE_URL + "middle/atom/index/list";
    public static final String SETUP_URL = BASE_URL + "middle/device/setup";
}
    
package com.hankun.ship.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.blankj.utilcode.util.CacheUtils;
import com.blankj.utilcode.util.Utils;
import com.hankun.ship.R;
import com.hankun.ship.net.HTTPSTrustManager;
import com.hankun.ship.net.TrustAllHostnameVerifier;
import com.hankun.ship.util.LogUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


/**
 * @author nan
 */
public class ShipApp extends Application {

    private static final boolean DEBUG_LOG = true;
    private static final String TAG = "ShipApp";

    /**
     * Shared preferences file name, where we store persistent values.
     */
    static final String SHARED_PREFS_FILENAME = "HanKun";
    public static final String FROM_NOTIFICATION = "1";


    private boolean isLogined = false; // 是否已登录
    private String account;//用户账号
    private String password;//用户密码
    private String ip;
    private String IMEI;
    private static ShipApp mApp;

    /**
     * 通道ID
     */
    private String mLane;

    /**
     * Shared preferences editor for writing program settings.
     */
    private SharedPreferences.Editor mPrefEditor = null;
    private SharedPreferences mSharedPrefs;

    /**
     * Http Client
     */
    private OkHttpClient mOKHttpClient;

    /**
     * Access token of Park
     */
    private String mToken;

    /**
     * Persistent value types.
     */
    public enum PersistentValueType {
        ACCOUNT,    //账号
        PASSWORD,
        IS_LOGIN,   //是否登录
        SERVICE_IP,  //服务器地址
    }

    private static ThreadPoolExecutor mExecutor;
    private static final int CORE_THREAD_NUM = 5;

    /**
     * 字体
     */
    private Typeface mTypeface;

    public static ShipApp getApp() {
        return mApp;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        //start log
        LogUtils.logInit(true);
        Utils.init(this);

        //获取字体
        mTypeface = getResources().getFont(R.font.comic);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(HTTPSTrustManager.allowAllSSL());
        builder.hostnameVerifier(new TrustAllHostnameVerifier());
        mOKHttpClient = builder.build();
        /*
         * Get shared preferences and editor so we can read/write program settings.
         */
        mSharedPrefs = getSharedPreferences(SHARED_PREFS_FILENAME, 0);
        mPrefEditor = mSharedPrefs.edit();

        this.isLogined = Boolean.valueOf(readValue(PersistentValueType.IS_LOGIN, "0"));
        this.account = readValue(PersistentValueType.ACCOUNT, "");
        this.password = readValue(PersistentValueType.PASSWORD, "");
    }

//    @SuppressLint({"MissingPermission", "HardwareIds"})
//    public String getIMEI() {
//        IMEI = null;
//        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
//        if (telephonyManager != null) {
//            IMEI = telephonyManager.getDeviceId();
//        } else {
//            Log.d(TAG, "getIMEI: have some error");
//        }
//        return IMEI;
//    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    /**
     * 由片段调用，设置登录信息
     *
     * @param login
     * @param account
     */
    public void setLogin(boolean login, String account, String password) {
        writePreferenceValue(PersistentValueType.IS_LOGIN, String.valueOf(login));
        writePreferenceValue(PersistentValueType.ACCOUNT, account);
        //不保存密码
        writePreferenceValue(PersistentValueType.PASSWORD, "");
        try {
            commitValues();
            this.isLogined = login;
            this.account = account;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogOut() {

        writePreferenceValue(PersistentValueType.IS_LOGIN, "");
        writePreferenceValue(PersistentValueType.ACCOUNT, "");
        writePreferenceValue(PersistentValueType.PASSWORD, "");

        try {
            commitValues();
            this.account = "";
            this.password = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the specified string persistent value.
     *
     * @param valueType - The persistent value type to read.
     * @param sDefault  - The default value to return if the requested value does not exist.
     * @return The requested value.
     */
    public String readValue(PersistentValueType valueType, String sDefault) {
        if (DEBUG_LOG) {
            Log.i(TAG, String.format("[readValue] ==> value [%s] sDefault [%s]",
                    valueType.toString(), sDefault));
        }

        String sValue = mSharedPrefs.getString(valueType.toString(), sDefault);

        if (DEBUG_LOG) {
            Log.i(TAG, "[readValue] <== nValue: " + sValue);
        }
        return sValue;
    }

    /**
     * Writes the specified string persistent value.
     *
     * @param valueType - The persistent value type to read.
     * @param sValue    - The value to write.
     */
    public void writePreferenceValue(PersistentValueType valueType, String sValue) {
        if (DEBUG_LOG) {
            Log.i(TAG, String.format("[writeValue] ==> sValuName [%s] nValue [%s]",
                    valueType.toString(), sValue));
        }

        mPrefEditor.putString(valueType.toString(), sValue);

        if (DEBUG_LOG) {
            Log.i(TAG, "[writeValue] <==");
        }
    }

    /**
     * Commits persistent values that were previously written.
     */
    public void commitValues() throws Exception {
        if (DEBUG_LOG) {
            Log.i(TAG, "[commitValues] ==>");
        }

        boolean bSuccess = mPrefEditor.commit();
        if (!bSuccess) {
            throw new Exception("commit() failed");
        }
        if (DEBUG_LOG) {
            Log.i(TAG, "[commitValues] <==");
        }
    }

    /**
     * Delete the specified string persistent value.
     *
     * @param valueType - The persistent value type to Delete.
     */
    public void deleteValue(PersistentValueType valueType) {
        if (DEBUG_LOG) {
            Log.i(TAG, String.format("[deleteValue] ==> sValuName [%s] ",
                    valueType.toString()));
        }
        mPrefEditor.remove(valueType.toString());

        if (DEBUG_LOG) {
            Log.i(TAG, "[deleteValue] <==");
        }
    }

    public OkHttpClient getOKHttpClient() {
        return mOKHttpClient;
    }

    public boolean isLogined() {
        return isLogined;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getServerIP() {
        return ip;
    }

    public void setServerIP(String ipStr) {
        writePreferenceValue(PersistentValueType.SERVICE_IP, ipStr);
        try {
            commitValues();
            this.ip = ipStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ThreadPoolExecutor getExecutor() {
        if (mExecutor == null) {
            //创建线程池
            mExecutor = new ThreadPoolExecutor(CORE_THREAD_NUM, 20, 500, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());
        }
        return mExecutor;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public String getLane() {
        return mLane;
    }

    public void setLane(String mLane) {
        this.mLane = mLane;
    }
}

package com.hankun.ship.net;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.hankun.ship.R;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.URL;
import com.hankun.ship.bean.response.ResponseData;
import com.hankun.ship.util.ShowMessage;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @author nan
 */
public class Network {
    private static String TAG = "nlgNetwork";
    @SuppressLint("StaticFieldLeak")
    private static Network mNetWork;
    @SuppressLint("StaticFieldLeak")
    private static Application mCtx;
    @SuppressLint("StaticFieldLeak")
    private static ThreadPoolExecutor executor;

    public static final int OK = 1;
    private static final int NG = 0;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private Network() {
    }

    public static Network Instance(ShipApp ctx) {
        if (mNetWork == null) {
            mCtx = ctx;
            executor = ctx.getExecutor();
            mNetWork = new Network();
        }
        return mNetWork;
    }

    /**
     * 判断是否有网络连接
     */
    public boolean isNetworkConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) (mCtx.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (connectivity != null) {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取数据信息
     */
    public void postData(final String url, final String jsonValue, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            Log.d(TAG, "Network is not ready.");
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && jsonValue != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(JSON, jsonValue);
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((ShipApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                ResponseData responseData = gson.fromJson(response.body().string(), ResponseData.class);
                                if (responseData != null) {
                                    Log.d(TAG, "postData responseData：" + responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = gson.toJson(responseData.getData());
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "postData Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                            }
                            response.close();
                        } catch (IOException e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
                        } catch (Exception e){
                            msg.what = NG;
                            msg.obj = "Json format error!";
                        }finally {
                            handler.sendMessage(msg);
                            if (response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取数据信息（GET）
     */
    public void getData(final String url, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            Log.d(TAG, "Network is not ready.");
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        //Post method
                        Request request = new Request.Builder().url(url).get().build();
                        OkHttpClient client = ((ShipApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                ResponseData responseData = gson.fromJson(response.body().string(), ResponseData.class);
                                if (responseData != null) {
                                    Log.d(TAG, "getData run: responseData：" + responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "getData Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                            }
                            response.close();
                        } catch (IOException e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
                        } catch (Exception e){
                            msg.what = NG;
                            msg.obj = "Json format error!";
                        }finally {
                            handler.sendMessage(msg);
                            if (response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }
}

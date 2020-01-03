package com.hankun.ship.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hankun.ship.R;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.URL;
import com.hankun.ship.bean.response.ParkResponseData;
import com.hankun.ship.net.Network;
import com.hankun.ship.ui.activity.SplashActivity;
import com.hankun.ship.util.ShowMessage;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author nan 2017/11/22
 */
public class MyMqttService extends Service {

    private static final String TAG = "nlgMqttService";
    private static final String TOPIC_MACHINE_STATUS_CHANGE = "/s2c/machine_status_change";
    private static final String TOPIC_TO_QA = "/s2c/task_quality/";
    private static final String TOPIC_TO_NEXT_INSTALL = "/s2c/task_install/";
    private static final String TOPIC_INSTALL_ABNORMAL_RESOLVE = "/s2c/install_abnormal_resolve/";
    private static final String TOPIC_QA_ABNORMAL_RESOLVE = "/s2c/quality_abnormal_resolve/";
    private static final String TOPIC_INSTALL_ABNORMAL = "/s2c/install_abnormal/";
    private static final String TOPIC_QUALITY_ABNORMAL = "/s2c/quality_abnormal/";
    /**
     * 发生安装异常时，通知对应质检员
     */
    public static final String TOPIC_INSTALL_ABNORMAL_TO_QUALITY = "/s2c/install_abnormal/quality/";

    private static final String publishTopic = "exampleAndroidPublishTopic";

    private MqttAndroidClient mqttAndroidClient;
    private NotificationManager mNotificationManager;
    private ShipApp mContext;
    private ThreadPoolExecutor mExecutor;
    private Network mNetwork;
    private Timer mUpdateTokenTimer = new Timer();
    private UpdateTokenTask mUpdateTokenTask = new UpdateTokenTask();

    public MyMqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = ShipApp.getApp();
        mNetwork = Network.Instance(mContext);
        //mUpdateTokenTimer.schedule(mUpdateTokenTask, 1000, 1000 * 60 * 60);

        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(ShipApp.FROM_NOTIFICATION, true);
        //这边设置“FLAG_UPDATE_CURRENT”是为了让后面的Activity接收pendingIntent中Extra的数据
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        createNotification(getApplication().getApplicationContext(), "DREAM CRUISES", "Click", pi);

    }

    /**
     * 订阅消息
     */
    public void subscribeToTopic(String subscriptionTopic) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Success to Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: Failed to subscribe");
                }
            });
        } catch (MqttException ex) {
            Log.d(TAG, "subscribeToTopic: Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    /**
     * 发布消息
     */
    public void publishMessage(String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Log.d(TAG, "publishMessage: Message Published: " + msg);
        } catch (MqttException e) {
            Log.d(TAG, "publishMessage: Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MqttService onStartCommand executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.disconnect();
            }
            if (mUpdateTokenTask != null) {
                mUpdateTokenTask.cancel();
                mUpdateTokenTask = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "MqttService onDestroy executed");
    }

    public void createNotification(Context context, String title, String content, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 通知渠道的id
        String id = "channel";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = "DREAM CRUISES";
        // 用户可以看到的通知渠道的描述
        String description = "";
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            //  配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
//            mChannel.enableLights(true);
            mChannel.setLightColor(Color.BLUE);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
            notificationBuilder = new NotificationCompat.Builder(context, id);
        } else {
            notificationBuilder = new NotificationCompat.Builder(context);
        }
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
        startForeground(1, notificationBuilder.build());
    }

    /**
     * 获取园区token
     */
    class UpdateTokenTask extends TimerTask {

        @Override
        public void run() {
            if (!mNetwork.isNetworkConnected()) {
                ShowMessage.showToast(mContext, mContext.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            } else {
                Map<String, String> requestObject = new HashMap<>();
                requestObject.put("username", "admin");
                requestObject.put("password", "21232f297a57a5a743894a0e4a801fc3");
                Gson gson = new Gson();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, gson.toJson(requestObject));
                //Post method
                Request request = new Request.Builder().addHeader("Content-Type", "application/json; charset=UTF-8")
                        .addHeader("Accept", "application/json")
                        .url(URL.PARK_URL + URL.LOGIN).post(body).build();

                OkHttpClient client = mContext.getOKHttpClient();
                Response response = null;
                try {
                    //同步网络请求
                    response = client.newCall(request).execute();
                    boolean success = false;
                    if (response.isSuccessful()) {
                        ParkResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ParkResponseData>() {
                        }.getType());
                        if (responseData != null) {
                            if (responseData.getRtn() == 0) {
                                String token = responseData.getResult();
                                Log.i(TAG, "Token update: " + token);
                                mContext.setToken(token);
                            } else {
                                Log.w(TAG, "Fetch token error, message: " + responseData.getMessage());
                                ShowMessage.showToast(mContext, "Fetch Token error! Message: " + responseData.getMessage(), ShowMessage.MessageDuring.SHORT);
                            }
                        }
                    }
                    response.close();
                } catch (Exception e) {
                    Log.e(TAG, "Fetch Token error!");

                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        }
    }
}

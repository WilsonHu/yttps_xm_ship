package com.hankun.ship.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.blankj.utilcode.util.DeviceUtils;
import com.google.gson.Gson;
import com.hankun.ship.R;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.Constants;
import com.hankun.ship.app.URL;
import com.hankun.ship.net.Network;
import com.hankun.ship.util.ShowMessage;

import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * @author nan 2017/11/15
 */
public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "SplashActivity";
    private static final int REQUEST_SOME_PERMISSIONS = 111;
    private static final String[] APP_NEEDS_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.FOREGROUND_SERVICE
    };

    private Network mNetwork;
    private FetchLoginHandler mFetchLoginHandler;
    private Handler mTimeoutHandler;
    private Runnable mTimeOutRunnable;

    private FaceEngine faceEngine = new FaceEngine();


    /**
     * @param 用于表明进入从notification 进入Splash Activity
     */
    private Boolean mJumpFromNotification = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags);
        setContentView(R.layout.activity_splash);
        Intent it = getIntent();
        mJumpFromNotification = it.getBooleanExtra(ShipApp.FROM_NOTIFICATION, false);
        mNetwork = Network.Instance(ShipApp.getApp());
        mFetchLoginHandler = new FetchLoginHandler();
        //申请权限
        requestSomePermissions();
    }

    @AfterPermissionGranted(REQUEST_SOME_PERMISSIONS)
    public void requestSomePermissions() {
        if (!EasyPermissions.hasPermissions(this, APP_NEEDS_PERMISSIONS)) {
            EasyPermissions.requestPermissions(
                    this,
                    "需打开以下权限",
                    REQUEST_SOME_PERMISSIONS,
                    APP_NEEDS_PERMISSIONS);
        } else {
            init();
            activeEngine(null);
        }
    }

    /**
     * 激活引擎
     *
     * @param view
     */
    public void activeEngine(final View view) {

        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                int activeCode = faceEngine.activeOnline(SplashActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                emitter.onNext(activeCode);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            ShowMessage.showToast(SplashActivity.this, getString(R.string.active_success), ShowMessage.MessageDuring.SHORT);
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            //ShowMessage.showToast(SplashActivity.this, getString(R.string.already_activated), ShowMessage.MessageDuring.SHORT);
                        } else {
                            ShowMessage.showToast(SplashActivity.this, getString(R.string.active_failed), ShowMessage.MessageDuring.SHORT);
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = faceEngine.getActiveFileInfo(SplashActivity.this,activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void init() {

        //检查preference中的isLogin状态
        boolean isLogin = ShipApp.getApp().isLogined();
        if (isLogin) {
            final String account = ShipApp.getApp().getAccount();
            final String password = ShipApp.getApp().getPassword();
            final String ip = ShipApp.getApp().getServerIP();
            //(1)检查账号密码是否存在
            if (account.isEmpty() || password.isEmpty()) {
                jumpToLoginAct();
            } else {
                //(2)检查网络连接是否正常
                if (!mNetwork.isNetworkConnected()) {
                    Toast.makeText(this, "网络无法连接，请检查！", Toast.LENGTH_LONG).show();
                    jumpToLoginAct();
                } else {
                    @SuppressLint("StaticFieldLeak") final AsyncTask task = new AsyncTask() {
                        @Override
                        protected void onCancelled() {
                            super.onCancelled();
                        }

                        @Override
                        protected Object doInBackground(Object[] params) {
                            //检查账号密码是否正确，正确的话返回流程的状态
                            LinkedHashMap<String, Object> postValue = new LinkedHashMap<>();
                            postValue.put("account", account);
                            postValue.put("password", password);
                            postValue.put("sn", DeviceUtils.getMacAddress());
                            String loginUrl = URL.USER_LOGIN;
                            mNetwork.postData(loginUrl, new Gson().toJson(postValue), mFetchLoginHandler);
                            return null;
                        }
                    };
                    task.execute();

                    //5秒内返回，从handler中移除runnable
                    mTimeoutHandler = new Handler();
                    mTimeOutRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //if timeout, finish async task, notify user the network error and jump to login activity
                            task.cancel(true);
                            Toast.makeText(SplashActivity.this, "网络连接错误超时", Toast.LENGTH_LONG).show();
                            jumpToLoginAct();
                        }
                    };
                    mTimeoutHandler.postDelayed(mTimeOutRunnable, 8000);
                }
            }
        } else {
            jumpToLoginAct();
        }
    }

    private void jumpToLoginAct() {
        final ImageView logoView = findViewById(R.id.skf_logo);
        // 设置加载动画透明度渐变从（0.1不显示-1.0完全显示）
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.1f);
        // 设置动画时间5s
        animation.setDuration(2000);
        // 将组件与动画关联
        logoView.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            // 动画开始时执行
            @Override
            public void onAnimationStart(Animation animation) {

            }

            // 动画重复时执行
            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            // 动画结束时执行
            @Override
            public void onAnimationEnd(Animation animation) {
                logoView.setVisibility(View.INVISIBLE);
                Intent it = new Intent();
                it.setClass(SplashActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
            }
        });
        logoView.startAnimation(animation);
    }

    @SuppressLint("HandlerLeak")
    private class FetchLoginHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            //网络请求返回，移除超时的Runnable
            mTimeoutHandler.removeCallbacks(mTimeOutRunnable);
            if (msg.what == Network.OK) {
                //为了在点击notification进入splash activity时有较好的体验，不显示动画
                if (mJumpFromNotification) {
                    Intent it = new Intent();
                    it.setClass(SplashActivity.this, SetupActivity.class);
                    startActivity(it);
                    finish();
                } else {
                    onFetchProcessDataSuccess();
                }
            } else {
                String errorMsg = (String) msg.obj;
                onFetchProcessDataFailed(errorMsg);
            }
        }
    }

    private void onFetchProcessDataSuccess() {
        final ImageView logoView = findViewById(R.id.skf_logo);
        // 设置加载动画透明度渐变从（0.1不显示-1.0完全显示）
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.1f);
        // 设置动画时间5s
        animation.setDuration(2000);
        // 将组件与动画关联
        logoView.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            // 动画开始时执行
            @Override
            public void onAnimationStart(Animation animation) {

            }

            // 动画重复时执行
            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            // 动画结束时执行
            @Override
            public void onAnimationEnd(Animation animation) {
                logoView.setVisibility(View.INVISIBLE);
                Intent it = new Intent();
                it.putExtra("lane", ShipApp.getApp().getAccount());
                it.setClass(SplashActivity.this, SetupActivity.class);
                startActivity(it);
                finish();
            }
        });
        logoView.startAnimation(animation);
    }
    private void onFetchProcessDataFailed(String errorMsg) {
        jumpToLoginAct();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            requestSomePermissions();
        }
    }
}

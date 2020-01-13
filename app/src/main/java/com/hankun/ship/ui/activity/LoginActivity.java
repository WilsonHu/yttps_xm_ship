package com.hankun.ship.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.DeviceUtils;
import com.google.gson.Gson;
import com.hankun.ship.R;
import com.hankun.ship.adapter.SearchAdapter;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.URL;
import com.hankun.ship.net.Network;
import com.hankun.ship.util.ShowMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Hu Tong 2019-07-24
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextView mSystemVersionTv,mIDLabelTv,mPasswordLabelTv;
    private AutoCompleteTextView mAccountText;
    private EditText mPasswordText;
    private Button mLoginButton;
    private ProgressDialog mLoadingProcessDialog;
    private Network mNetwork;
    private LoginHandler mLoginHandler;
    private ShipApp mApp;
    private AlertDialog mIPSettingDialog = null;

    private FetchDeviceListHandler mFetchDeviceListHandler;

    /**
     * 设备列表
     */
    private ArrayList<String> mDeviceList = new ArrayList<>();
    private SearchAdapter<String> mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_login);

        mApp = (ShipApp) getApplication();
        mNetwork = Network.Instance(mApp);

        mIDLabelTv = findViewById(R.id.id_label);
        mIDLabelTv.setTypeface(ShipApp.getApp().getTypeface());

        mPasswordLabelTv = findViewById(R.id.password_label);
        mPasswordLabelTv.setTypeface(ShipApp.getApp().getTypeface());

        mPasswordText = findViewById(R.id.input_password);
        mPasswordText.setTypeface(mApp.getTypeface());
        mAccountText = findViewById(R.id.input_account);
        mAccountText.setTypeface(mApp.getTypeface());
        if (ShipApp.getApp().getAccount() != null && !"".equals(ShipApp.getApp().getAccount())) {
            mAccountText.setText(ShipApp.getApp().getAccount());
        }
        mSearchAdapter = new SearchAdapter<>(LoginActivity.this, android.R.layout.simple_list_item_1, mDeviceList, SearchAdapter.ALL);
        mAccountText.setAdapter(mSearchAdapter);

        mSystemVersionTv = findViewById(R.id.system_version);
        mSystemVersionTv.setText(getVersion());
        mSystemVersionTv.setTypeface(mApp.getTypeface());

        mLoginHandler = new LoginHandler();
        mLoginButton = findViewById(R.id.btn_login);
        mLoginButton.setTypeface(mApp.getTypeface());
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFetchDeviceListHandler = new FetchDeviceListHandler();
        mNetwork.getData(URL.DEVICE_URL,mFetchDeviceListHandler);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return "v" + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "No Version";
        }
    }

    private void login() {
        String password = mPasswordText.getText().toString();
        String account = mAccountText.getText().toString();
        mLoginButton.setEnabled(false);
        if (mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(LoginActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("Loading...");
        }
        mLoadingProcessDialog.show();
        LinkedHashMap<String, Object> postValue = new LinkedHashMap<>();
        postValue.put("account", account);
        postValue.put("password", password);
        postValue.put("sn", DeviceUtils.getMacAddress());
        mNetwork.postData(URL.USER_LOGIN, new Gson().toJson(postValue), mLoginHandler);
//        Intent it = new Intent();
//        it.putExtra("lane", account);
//        it.setClass(LoginActivity.this, SetupActivity.class);
//        startActivity(it);
    }

    @SuppressLint("HandlerLeak")
    class LoginHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                onLoginSuccess(msg.obj);
            } else {
                String errorMsg = (String) msg.obj;
                onLoginFailed(errorMsg);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class FetchDeviceListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                if (msg.obj instanceof ArrayList<?>) {
                    for (Object o : (List<?>) msg.obj) {
                        mDeviceList.add(String.class.cast(o));
                    }
                }
                mSearchAdapter = new SearchAdapter<>(LoginActivity.this, android.R.layout.simple_list_item_1, mDeviceList, SearchAdapter.ALL);
                mAccountText.setAdapter(mSearchAdapter);
                mSearchAdapter.notifyDataSetChanged();
            } else {
                final String errorMsg = (String) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(LoginActivity.this,errorMsg, ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        }
    }

    public void onLoginSuccess(Object data) {
        mLoginButton.setEnabled(true);
        if (data != null) {
            mApp.setLogin(true, mAccountText.getText().toString(), mPasswordText.getText().toString());
            Intent it = new Intent();
            mApp.setLane(mAccountText.getText().toString());
            it.setClass(LoginActivity.this, SetupActivity.class);
            startActivity(it);
            finish();
        }
    }

    public void onLoginFailed(String msg) {
        if (msg != null) {
            ShowMessage.showDialog(LoginActivity.this, msg, null);
        } else {
            ShowMessage.showDialog(LoginActivity.this, "Network error.", null);
        }
        mLoginButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadingProcessDialog != null) {
            mLoadingProcessDialog.dismiss();
        }
        if (mIPSettingDialog != null) {
            mIPSettingDialog.dismiss();
        }
    }
}

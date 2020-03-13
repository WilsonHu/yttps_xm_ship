package com.hankun.ship.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.blankj.utilcode.util.DeviceUtils;
import com.google.gson.Gson;
import com.hankun.ship.R;
import com.hankun.ship.adapter.MySpinnerAdapter;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.URL;
import com.hankun.ship.bean.ModeType;
import com.hankun.ship.net.Network;
import com.hankun.ship.util.ShowMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * @author HT  2019/12/24
 */
public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "SetupActivity";

    /**
     * 特殊字体
     */
    private Typeface mTypeface;

    private TextView mSetupTitleTv, mVesselTv, mVesselValueTv, mCruiseTv, mLaneTv, mLaneValueTv, mSelectModeTv;
    private Button mEmbarkBtn, mDisEmbarkBtn, mShorexDisembarkBtn, mShorexReembarkBtn;
    private Spinner mCruiseValueSp;

    private ArrayList<String> mCruiseIDList = new ArrayList<>();
    private FetchCruiseIDHandler mFetchCruiseIDHandler;
    private MySpinnerAdapter mySpinnerAdapter;

    private UploadModeHandler mUploadModeHandler;

    private LogoutHandler mLogoutHandler;

    /**
     * Cruise ID
     */
    private String mCruiseID = "";

    private Network mNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags);
        setContentView(R.layout.activity_setup);

        //获取字体
        mTypeface = ShipApp.getApp().getTypeface();

        mSetupTitleTv = findViewById(R.id.setup_title_tv);
        mVesselTv = findViewById(R.id.setup_vessel_tv);
        mVesselValueTv = findViewById(R.id.setup_vessel_value_tv);
        mCruiseTv = findViewById(R.id.setup_cruiseid_tv);
        mCruiseValueSp = findViewById(R.id.setup_cruiseid_value_sp);
        mLaneTv = findViewById(R.id.setup_lane_tv);
        mLaneValueTv = findViewById(R.id.setup_lane_value_tv);
        mLaneValueTv.setText(ShipApp.getApp().getLane());
        mSelectModeTv = findViewById(R.id.setup_select_mode_tv);

        mEmbarkBtn = findViewById(R.id.setup_embark_btn);
        mEmbarkBtn.setOnClickListener(this);
        mDisEmbarkBtn = findViewById(R.id.setup_disembark_btn);
        mDisEmbarkBtn.setOnClickListener(this);
        mShorexDisembarkBtn = findViewById(R.id.shorex_disembark_btn);
        mShorexDisembarkBtn.setOnClickListener(this);
        mShorexReembarkBtn = findViewById(R.id.shorex_reembark_btn);
        mShorexReembarkBtn.setOnClickListener(this);
        setTypeface();
        mySpinnerAdapter = new MySpinnerAdapter(this, mCruiseIDList);
        mCruiseValueSp.setAdapter(mySpinnerAdapter);
        mCruiseValueSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mCruiseIDList.get(position).length() >= 2) {
                    mVesselValueTv.setText(mCruiseIDList.get(position).substring(0, 2) + "R");
                    mCruiseID = mCruiseIDList.get(position);
                    setModeBtnStatus(true);
                } else {
                    setModeBtnStatus(false);
                    mVesselValueTv.setText("");
                    mCruiseID = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setModeBtnStatus(false);
        mNetwork = Network.Instance(ShipApp.getApp());
        mLogoutHandler = new LogoutHandler();
    }


    /**
     * 设置button状态
     * @param enable
     */
    private void setModeBtnStatus(boolean enable) {
        mEmbarkBtn.setClickable(enable);
        mDisEmbarkBtn.setClickable(enable);
        mShorexDisembarkBtn.setClickable(enable);
        mShorexReembarkBtn.setClickable(enable);
    }

    private void setTypeface() {
        mSetupTitleTv.setTypeface(mTypeface);
        mVesselTv.setTypeface(mTypeface);
        mVesselValueTv.setTypeface(mTypeface);
        mCruiseTv.setTypeface(mTypeface);
        mLaneTv.setTypeface(mTypeface);
        mLaneValueTv.setTypeface(mTypeface);
        mSelectModeTv.setTypeface(mTypeface);

        mEmbarkBtn.setTypeface(mTypeface);
        mDisEmbarkBtn.setTypeface(mTypeface);
        mShorexDisembarkBtn.setTypeface(mTypeface);
        mShorexReembarkBtn.setTypeface(mTypeface);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFetchCruiseIDHandler = new FetchCruiseIDHandler();
        mNetwork.getData(URL.CRUISE_URL, mFetchCruiseIDHandler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        //在设置界面，不显示setting
        menu.getItem(0).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Logout")) {
            mNetwork.getData(URL.LOGOUT + mLaneValueTv.getText(), mLogoutHandler);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if("".equals(mCruiseID)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMessage.showToast(SetupActivity.this,"Cruise ID is empty.", ShowMessage.MessageDuring.SHORT);
                }
            });
        } else {
            String mode = "";
            switch (v.getId()) {
                case R.id.setup_embark_btn:
                    mode = ModeType.EMBARK;
                    break;
                case R.id.setup_disembark_btn:
                    mode = ModeType.DISEMBARK;
                    break;
                case R.id.shorex_disembark_btn:
                    mode = ModeType.SHOREX_DISEMBARK;
                    break;
                case R.id.shorex_reembark_btn:
                    mode = ModeType.SHOREX_EMBARK;
                    break;
            }
            if (!mode.equals("")) {
                mUploadModeHandler = new UploadModeHandler();
                LinkedHashMap<String, Object> postValue = new LinkedHashMap<>();
                postValue.put("cruise_id", mCruiseID);
                postValue.put("lane", mLaneValueTv.getText());
                postValue.put("mode", mode);
                postValue.put("sn", DeviceUtils.getMacAddress());
                mNetwork.postData(URL.SETUP_URL, new Gson().toJson(postValue), mUploadModeHandler);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class FetchCruiseIDHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                mCruiseIDList = new ArrayList<>();
                mCruiseIDList.add("");
                if (msg.obj instanceof ArrayList<?>) {
                    for (Object o : (List<?>) msg.obj) {
                        mCruiseIDList.add(String.class.cast(o));
                    }
                }
                mySpinnerAdapter = new MySpinnerAdapter(SetupActivity.this, mCruiseIDList);
                mCruiseValueSp.setAdapter(mySpinnerAdapter);
                mySpinnerAdapter.notifyDataSetChanged();

            } else {
                final String errorMsg = (String) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(SetupActivity.this,errorMsg, ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        }
    }

    /**
     * 上传设置参数callback
     */
    @SuppressLint("HandlerLeak")
    class UploadModeHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Intent it = new Intent();
                it.setClass(SetupActivity.this, FaceScanActivity.class);
                it.putExtra("cruise_id", mCruiseID);
                it.putExtra("lane", mLaneValueTv.getText());
                it.putExtra("vessel", mVesselValueTv.getText());
                startActivity(it);
                finish();
            } else {
                final String errorMsg = (String) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(SetupActivity.this,errorMsg, ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        }
    }

    @SuppressLint("HandlerLeak")
    class LogoutHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                ShipApp.getApp().setLogOut();
                ShipApp.getApp().setLane("");
                Intent it = new Intent();
                it.setClass(SetupActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
            } else {
                final String errorMsg = (String) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(SetupActivity.this, errorMsg, ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        }
    }
}

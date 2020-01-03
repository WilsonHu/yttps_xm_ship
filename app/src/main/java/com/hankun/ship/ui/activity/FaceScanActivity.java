package com.hankun.ship.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.VersionInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hankun.ship.R;
import com.hankun.ship.adapter.SearchAdapter;
import com.hankun.ship.app.ShipApp;
import com.hankun.ship.app.URL;
import com.hankun.ship.bean.DrawInfo;
import com.hankun.ship.bean.FaceDetectDTO;
import com.hankun.ship.bean.ModeType;
import com.hankun.ship.bean.response.ResponseData;
import com.hankun.ship.net.Network;
import com.hankun.ship.service.MyMqttService;
import com.hankun.ship.util.BaseUtil;
import com.hankun.ship.util.ConfigUtil;
import com.hankun.ship.util.DrawHelper;
import com.hankun.ship.util.ShowMessage;
import com.hankun.ship.util.camera.CameraHelper;
import com.hankun.ship.util.camera.CameraListener;
import com.hankun.ship.widget.FaceRectView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.arcsoft.face.LivenessInfo.UNKNOWN;

public class FaceScanActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "FaceScanActivity";
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer rgbCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private FaceEngine faceEngine;
    private int afCode = -1;
    //    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    private int processMask = FaceEngine.ASF_FACE3DANGLE;
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    private FaceRectView faceRectView;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    private static boolean PROCESSING = false;

    private static int CURRENT_DETECTING_FACE_ID = -1;

    private LinearLayout mLoadingDialog;
    private LinearLayout mVerifyResultDialog;
    private TextView mNoticeMessage;

    /**
     * 识别处理线程
     */
    private Thread mRecoThread;

    private static final ConcurrentLinkedQueue<PreviewData> FACE_PROCESS_LIST = new ConcurrentLinkedQueue<>();

    private DrawInfo mDrawInfo;
    private MediaPlayer mFailMediaPlayer;
    private MediaPlayer mSuccessMediaPlayer;
    private MediaPlayer mNotifyRecordMediaPlayer;

    private Intent mqttIntent;

    /**
     * 人像库
     */
    private String mCruiseID;

    /**
     * 通道ID
     */
    private String mLane;

    /**
     * Vessel
     */
    private String mVessel;

    private Network mNetwork;

    private LogoutHandler mLogoutHandler;

    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    public FaceScanActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_preview);

        mCruiseID = getIntent().getStringExtra("cruise_id");
        mLane = ShipApp.getApp().getLane();
        mVessel = getIntent().getStringExtra("vessel");

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        previewView = findViewById(R.id.texture_preview);
        faceRectView = findViewById(R.id.face_rect_view);
        mNoticeMessage = findViewById(R.id.notice_message);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (mRecoThread == null) {
            mRecoThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    doProcessRecognize();
                }
            });
            mRecoThread.start();
        }
        PROCESSING = true;

        initDialog();
        initMediaPlayer();
        //启动MQTT服务
        mqttIntent = new Intent(this, MyMqttService.class);
        startForegroundService(mqttIntent);

        mLogoutHandler = new LogoutHandler();
        mNetwork = Network.Instance(ShipApp.getApp());
    }

    private void initEngine() {
//        faceEngine = new FaceEngine();
//        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
//                16, 20, FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                4, 1, FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE3DANGLE);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);
        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }
    }

    private void unInitEngine() {

        if (afCode == 0) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cameraHelper != null && cameraHelper.isStopped()) {
            cameraHelper.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //PROCESSING = false;
        cameraHelper.stop();
    }

    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        unInitEngine();
        if (mRecoThread != null && mRecoThread.isAlive()) {
            mRecoThread.interrupt();
            mRecoThread = null;
        }
        if (mSuccessMediaPlayer != null) {
            mSuccessMediaPlayer.stop();
            mSuccessMediaPlayer = null;
        }
        if (mFailMediaPlayer != null) {
            mFailMediaPlayer.stop();
            mFailMediaPlayer = null;
        }
        if (mNotifyRecordMediaPlayer != null) {
            mNotifyRecordMediaPlayer.stop();
            mNotifyRecordMediaPlayer = null;
        }
        stopService(mqttIntent);
        PROCESSING = false;
        super.onDestroy();
    }

    private boolean checkPermissions() {
        boolean allGranted = true;
        for (String neededPermission : NEEDED_PERMISSIONS) {
            allGranted &= ContextCompat.checkSelfPermission(this.getApplicationContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isMirror);
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);
            }

            @Override
            public void onPreview(byte[] nv21, Camera camera) {

                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                //对话框显示时不识别
                if (mVerifyResultDialog.isShown()) {
                    return;
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code == ErrorInfo.MOK && faceInfoList.size() == 1) {
                    if (faceInfoList.get(0).getFaceId() == CURRENT_DETECTING_FACE_ID) {
                        if (mDrawInfo != null) {
                            List<DrawInfo> drawInfoList = new ArrayList<>();
                            drawInfoList.add(mDrawInfo);
                            drawHelper.draw(faceRectView, drawInfoList);
                        }
                        return;
                    } else {
                        CURRENT_DETECTING_FACE_ID = faceInfoList.get(0).getFaceId();
                    }
                    code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    if (code != ErrorInfo.MOK) {
                        CURRENT_DETECTING_FACE_ID = -1;
                        return;
                    }
                    final List<Face3DAngle> face3DAngleList = new ArrayList<>();
                    int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
                    //有其中一个的错误码不为0，return
                    if (face3DAngleCode != ErrorInfo.MOK) {
                        CURRENT_DETECTING_FACE_ID = -1;
                        return;
                    }
                    final String result = faceIsValid(face3DAngleList.get(0), faceInfoList.get(0));
                    if (result != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNoticeMessage.setText(result);
                            }
                        });
                        CURRENT_DETECTING_FACE_ID = -1;
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNoticeMessage.setText("");
                            }
                        });
                        if (faceRectView != null && drawHelper != null) {
                            byte[] copyByte = new byte[nv21.length];
                            System.arraycopy(nv21, 0, copyByte, 0, nv21.length);
                            PreviewData previewData = new PreviewData(new DrawInfo(drawHelper.adjustRect(faceInfoList.get(0).getRect()), GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, UNKNOWN, null), copyByte, faceInfoList.get(0));
                            FACE_PROCESS_LIST.add(previewData);
                        }
                    }
                } else {
                    previewView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mNoticeMessage.getText().equals("")) {
                                mNoticeMessage.setText("");
                            }
                        }
                    }, 10);
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraId != null ? rgbCameraId : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }


    private String faceIsValid(Face3DAngle face3DAngle, FaceInfo faceInfo) {
        String result = null;
        //左右侧脸25度内，低抬头15度内
        if (Math.abs(face3DAngle.getYaw()) - 20 > 0 || Math.abs(face3DAngle.getPitch()) - 15 > 0) {
            result = "Please face the camera in front.";
        }

        if (faceInfo.getRect().left <= 10 || faceInfo.getRect().right > previewSize.width ||
                faceInfo.getRect().top <= 10 || faceInfo.getRect().bottom > previewSize.height) {
            result = "Please keep your face in camera.";
        }

        //判断人脸比例在camera的中比例
        float faceRate = faceInfo.getRect().width() / (float) previewSize.width;
        if (faceRate < 0.2) {
            result = "Please close to the camera.";
        }

        return result;
    }

    private void doProcessRecognize() {

        while (PROCESSING) {
            try {
                if (FACE_PROCESS_LIST.size() > 0) {
                    //每十帧处理一次人脸，华为的camera的速度是每秒29帧
                    //当size大于4时，表示请求园区进行识别的速度跟不上
                    while (FACE_PROCESS_LIST.size() > 4) {
                        FACE_PROCESS_LIST.poll();
                    }
                    if (FACE_PROCESS_LIST.size() > 0) {
                        PreviewData data = FACE_PROCESS_LIST.poll();
                        if (data == null) {
                            Log.e(TAG, "Preview camera data is null!");
                            CURRENT_DETECTING_FACE_ID = -1;
                            continue;
                        }
                        checkByRemote(data);
                    }
                    Log.i(TAG, "FACE_PROCESS_LIST count: " + FACE_PROCESS_LIST.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Process exception!");
            }
        }
    }

    void checkByRemote(PreviewData data) {
        Log.w(TAG, "Begin to remote compare");
        String base64Str = BaseUtil.getBase64FromYUV(data.cameraData, previewSize.width, previewSize.height);
        Log.w(TAG, "Finish picture process.");
        Response response = null;
        try {
            FaceDetectDTO faceDetectDTO = new FaceDetectDTO();
            faceDetectDTO.setImageBase64(base64Str);
            HashMap<String, String> attributesHashMap = new HashMap<>();
            attributesHashMap.put("sn", ShipApp.getApp().getIMEI());
            attributesHashMap.put("cruise_id", mCruiseID);
            attributesHashMap.put("lane", mLane);
            attributesHashMap.put("vessel", mVessel);

            faceDetectDTO.setAttributes(attributesHashMap);
            final Gson gson = new Gson();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, gson.toJson(faceDetectDTO));
            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/json; charset=UTF-8")
                    .addHeader("Accept", "application/json")
                    .url(URL.FACE_DETECT).post(body).build();
            OkHttpClient client = ShipApp.getApp().getOKHttpClient();
            //同步网络请求
            long beginTime = System.currentTimeMillis();
            Log.w(TAG, "Request begin: " + beginTime);
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                final ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>() {
                }.getType());
                if (responseData != null) {
                    if (responseData.getCode() == 200 || responseData.getCode() == 400) {
                        String okStr = responseData.getData().toString();
                        List<String> results = Arrays.asList(okStr.split("\\n"));
                        if (results != null && results.size() == 4) {
                            showVerifyResultDialog(true, results);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowMessage.showToast(FaceScanActivity.this, "Result format error!", ShowMessage.MessageDuring.SHORT);
                                }
                            });
                        }
                        if (responseData.getCode() == 400) {
                            showVerifyResultDialog(false, results);
                        } else {
                            showVerifyResultDialog(true, results);
                        }
                    }
                } else {
                    showVerifyResultDialog(false, null);
                }
                Long endTime = System.currentTimeMillis();
                Log.w(TAG, "Request end: " + endTime);
                Log.w(TAG, "Request cost: " + (endTime - beginTime));
            } else {
                //网络连接错误
                showVerifyResultDialog(false, null);
            }
        } catch (Exception e) {
            Log.w(TAG, "Remote compare error!");
            e.printStackTrace();
            //网络连接错误
            showVerifyResultDialog(false, null);
        } finally {
            if (response != null) {
                response.close();
            }
            //CURRENT_DETECTING_FACE_ID = -1;
            Log.w(TAG, "Remote compare complete");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
                if (cameraHelper != null) {
                    cameraHelper.start();
                }
            } else {
                ShowMessage.showToast(ShipApp.getApp(), getString(R.string.permission_denied), ShowMessage.MessageDuring.SHORT);
            }
        }
    }

    /**
     * 识别结果对话框
     */
    private void showVerifyResultDialog(final boolean pass, final List<String> showList) {

        previewView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLoadingDialog.isShown()) {
                    mLoadingDialog.setVisibility(View.INVISIBLE);
                }
                mVerifyResultDialog.setVisibility(View.VISIBLE);
                ImageView verifyResultIv = mVerifyResultDialog.findViewById(R.id.verify_result_icon);
                TextView verifyEng = mVerifyResultDialog.findViewById(R.id.verify_result_en);
                TextView verifyTc = mVerifyResultDialog.findViewById(R.id.verify_result_tc);
                TextView verifySc = mVerifyResultDialog.findViewById(R.id.verify_result_sc);
                TextView verifyJa = mVerifyResultDialog.findViewById(R.id.verify_result_ja);
                if (pass) {
                    verifyResultIv.setImageResource(R.drawable.verify_success);
                    verifyEng.setText(showList.get(0));
                    verifyTc.setText(showList.get(1));
                    verifySc.setText(showList.get(2));
                    verifyJa.setText(showList.get(3));
                } else {
                    verifyResultIv.setImageResource(R.drawable.verify_fail);
                    if (showList != null && showList.size() == 4) {
                        verifyEng.setText(showList.get(0));
                        verifyTc.setText(showList.get(1));
                        verifySc.setText(showList.get(2));
                        verifyJa.setText(showList.get(3));
                    } else {
                        verifyEng.setText("Please seek for staff assistance");
                        verifyTc.setText("請尋求員工協助");
                        verifySc.setText("请寻求员工协助");
                        verifyJa.setText("スタッフの支援を求めてください");
                    }
                }
            }
        }, 10);
        previewView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mVerifyResultDialog.isShown()) {
                    mVerifyResultDialog.setVisibility(View.INVISIBLE);
                    mLoadingDialog.setVisibility(View.VISIBLE);
                    CURRENT_DETECTING_FACE_ID = -1;
                }
            }
        }, 3000);
    }

    // 初始化MediaPlayer
    private void initMediaPlayer() {
        if (mFailMediaPlayer == null) {
            mFailMediaPlayer = MediaPlayer.create(this, R.raw.failed);
            // 设置音量，参数分别表示左右声道声音大小，取值范围为0~1
            mFailMediaPlayer.setVolume(1f, 1f);
            // 设置是否循环播放
            mFailMediaPlayer.setLooping(false);
        }
        if (mSuccessMediaPlayer == null) {
            mSuccessMediaPlayer = MediaPlayer.create(this, R.raw.success);
            // 设置音量，参数分别表示左右声道声音大小，取值范围为0~1
            mSuccessMediaPlayer.setVolume(1f, 1f);
            // 设置是否循环播放
            mSuccessMediaPlayer.setLooping(false);
        }
        if (mNotifyRecordMediaPlayer == null) {
            mNotifyRecordMediaPlayer = MediaPlayer.create(this, R.raw.record_notify);
            // 设置音量，参数分别表示左右声道声音大小，取值范围为0~1
            mNotifyRecordMediaPlayer.setVolume(1f, 1f);
            // 设置是否循环播放
            mNotifyRecordMediaPlayer.setLooping(false);
        }
    }

    private void initDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = findViewById(R.id.loading_dialog);
            TextView loadingEng = mLoadingDialog.findViewById(R.id.loading_en);
            loadingEng.setTypeface(ShipApp.getApp().getTypeface());
        }
        if (mVerifyResultDialog == null) {
            mVerifyResultDialog = findViewById(R.id.verify_dialog);
            TextView loadingEng = mVerifyResultDialog.findViewById(R.id.verify_result_en);
            loadingEng.setTypeface(ShipApp.getApp().getTypeface());
        }
    }


    /**
     * 在{@link #previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }

    class PreviewData {
        DrawInfo drawInfo;
        byte[] cameraData;
        FaceInfo faceInfo;

        PreviewData(DrawInfo drawInfo, byte[] cameraData, FaceInfo faceInfo) {
            this.cameraData = cameraData;
            this.drawInfo = drawInfo;
            this.faceInfo = faceInfo;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Logout")) {
            mNetwork.getData(URL.LOGOUT + mLane, mLogoutHandler);
        } else if (item.getTitle().equals("Setting")) {
            Intent it = new Intent();
            it.setClass(FaceScanActivity.this, SetupActivity.class);
            startActivity(it);
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HandlerLeak")
    class LogoutHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                ShipApp.getApp().setLogOut();
                ShipApp.getApp().setLane("");
                Intent it = new Intent();
                it.setClass(FaceScanActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
            } else {
                final String errorMsg = (String) msg.obj;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(FaceScanActivity.this, errorMsg, ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        }
    }
}

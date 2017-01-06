package com.jy.yisaidemo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.jy.jylibrary.base.BaseActivity;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.CommonUtils;
import util.FileUtils;

import static com.blankj.utilcode.utils.ScreenUtils.getScreenHeight;
import static com.blankj.utilcode.utils.ScreenUtils.getScreenWidth;
import static util.CommonUtils.SIZE_1;
import static util.CommonUtils.SIZE_2;

/**
 * Created by Administrator on 2016/12/28.
 */

public class RecordActivity extends BaseActivity implements SurfaceHolder.Callback {
    private static final String TAG = "RecordActivity";
    @BindView(R.id.camera_show_view)
    SurfaceView mCameraShowView;
    @BindView(R.id.video_flash_light)
    ImageView mVideoFlashLight;
    @BindView(R.id.video_time)
    Chronometer mVideoTime;
    @BindView(R.id.swicth_camera)
    ImageView mSwicthCamera;
    @BindView(R.id.record_button)
    ImageView mRecordButton;

    MediaRecorder mRecorder;
    SurfaceHolder mSurfaceHolder;
    Camera mCamera;
    OrientationEventListener mOrientationEventListener;
    File viderFile;

    int rotationRecord = 90;
    int rotationFlag = 90;
    int flashType;
    int frontRotate;
    int frontOri;
    int cameraType = 0;
    int cameraFlag = 1;//1为后置
    boolean flagRecord = false;//是否正在录像


    @Override
    protected int getContentId() {
        return R.layout.layout_shoot;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (flagRecord) {
                endRecord();
                if (mCamera != null && cameraType == 0) {
                    //关闭后置摄像头闪光灯
                    mCamera.lock();
                    FlashLogic(mCamera.getParameters(), 0, true);
                    mCamera.unlock();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        endRecordUI();
    }

    @Override
    public void onBackPressed() {
        if (flagRecord){
            //如果是录制中的就完成录制
            onPause();
            return;
        }
        super.onBackPressed();
    }


    /**
     * 闪光灯逻辑
     *
     * @param p 相机参数
     * @param type       打开还是关闭
     * @param isOn       是否启动前置摄像头
     */
    private void FlashLogic(Camera.Parameters p, int type, boolean isOn) {
        flashType = type;
        if (type == 0) {
            if (isOn) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
            }
            mVideoFlashLight.setImageResource(R.drawable.flash_off);
        } else {
            if (isOn) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
            }
            mVideoFlashLight.setImageResource(R.drawable.flash);
        }
        if (cameraFlag == 0) {
            mVideoFlashLight.setVisibility(View.GONE);
        } else {
            mVideoFlashLight.setVisibility(View.VISIBLE);
        }
    }


    private void endRecord() {
        //反正多次进入，比如surface的destroy和界面的onPause
        if (!flagRecord) {
            return;
        }
        flagRecord = false;
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mOrientationEventListener.enable();
                mRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVideoTime.stop();
        mVideoTime.setBase(SystemClock.elapsedRealtime());
        Intent intent = new Intent(this, PlayActivity.class);
        intent.putExtra(PlayActivity.VIDEO_URL, viderFile.getAbsolutePath());
        startActivityForResult(intent, 2222);
        overridePendingTransition(R.anim.design_fab_in, R.anim.design_fab_out);
    }

    @Override
    protected void initView() {
        super.initView();
        doStartSize();
        SurfaceHolder holder = mCameraShowView.getHolder();
        holder.addCallback(this);
        //setType必须设置，不然报错
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        roationUIListener();
    }

    /**
     * 因为录制改分辨率的比例可能和屏幕比例一直，所以需要调整比例显示
     */
    private void doStartSize() {
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();
        CommonUtils.setViewSize(mCameraShowView, screenWidth * SIZE_1 / CommonUtils.SIZE_2, screenHeight);
    }


    /**
     * 旋转界面UI
     */
    private void roationUIListener() {
        mOrientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    //竖屏拍摄
                    if (rotationFlag != 0) {
                        //旋转logo
                        rotationAnimation(rotationFlag, 0);
                        //这是竖屏视频需要的角度
                        rotationRecord = 90;
                        rotationFlag = 0;
                    }
                } else if ((rotation >= 230) && (rotation <= 310)) {
                    //横屏拍摄
                    if (rotationFlag != 90) {
                        //旋转logo
                        rotationAnimation(rotationFlag, 90);
                        //这是横屏视频需要的角度
                        rotationFlag = 90;
                    }
                } else if (rotation > 30 && rotation < 95) {
                    //反横屏拍摄
                    if (rotationFlag != 270) {
                        //旋转logo
                        rotationAnimation(rotationFlag, -90);
                        //这是横屏视频需要的角度
                        rotationRecord = 180;
                        //这是记录当前角度的flag
                        rotationFlag = -90;
                    }
                }
            }
        };
        mOrientationEventListener.enable();
    }

    private void rotationAnimation(int from, int to) {
        ValueAnimator progressAnimator = ValueAnimator.ofInt(from, to);
        progressAnimator.setDuration(300);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int currentAngle = (int) valueAnimator.getAnimatedValue();
                mVideoFlashLight.setRotation(currentAngle);
                mVideoTime.setRotation(currentAngle);
                mSwicthCamera.setRotation(currentAngle);
            }
        });
        progressAnimator.start();
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;
        initCamera(cameraType,false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        endRecord();
        releaseCamera();
    }

    @OnClick({R.id.video_flash_light, R.id.swicth_camera, R.id.record_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_flash_light:
                Log.d(TAG, "onClick: ");
                clickFlash();
                break;
            case R.id.swicth_camera:
                switchCamera();
                break;
            case R.id.record_button:
                clickRecord();
                break;
        }
    }

    /**
     * 录制按键
     */
    private void clickRecord() {
        if (!flagRecord) {
            if (startRecord()) {
                startRecordUI();
                mVideoTime.setBase(SystemClock.elapsedRealtime());
                mVideoTime.start();
                mVideoTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        //录制时间
                        if (chronometer.getText().equals("00:05")){
                            if (flagRecord){
                                endRecord();
                            }
                        }
                    }
                });
            }
        }else {
            endRecord();
        }
    }

    /**
     * 开始录制时候的状态
     */
    private void startRecordUI() {
        mSwicthCamera.setVisibility(View.GONE); // 旋转摄像头关闭
        mVideoFlashLight.setVisibility(View.GONE); //闪光灯关闭
        mRecordButton.setImageResource(R.drawable.stop_record); //录制按钮变成待停止
    }



    private boolean startRecord() {
        //懒人模式，根据闪光灯和摄像头前后重新初始化一遍，开期闪光灯工作模式
        initCamera(cameraType, true);

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || mCamera == null
                || mRecorder == null) {
            mCamera = null;
            mRecorder = null;
            showCameraPermission();
            return false;
        }
        try {
            mRecorder.setCamera(mCamera);
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // 设置输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 设置帧数，必须在setEncoder之前
            mRecorder.setVideoFrameRate(15);
            mRecorder.setVideoSize(SIZE_1, SIZE_2);

            //这两项需要放在setOutputFormat之后
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mRecorder.setVideoEncodingBitRate(3 * SIZE_1 * SIZE_2);

            int frontRotation;
            if (rotationRecord == 180) {
                //反向的前置
                frontRotation = 180;
            } else {
                //正向的前置
                frontRotation = (rotationRecord == 0) ? 270 - frontOri : frontOri; //录制下来的视屏选择角度，此处为前置
            }
            mRecorder.setOrientationHint((cameraType == 1) ? frontRotation:rotationRecord);
            //把摄像头的画面给他
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            //创建好视频文件来保存
            videoDir();
            if (viderFile!=null){
                //设置创建好的路径
                mRecorder.setOutputFile(viderFile.getPath());
                mRecorder.prepare();
                mRecorder.start();
                //不能旋转了
                mOrientationEventListener.disable();
                flagRecord = true;
            }

        } catch (Exception e) {
            //一般没有录制权限或者录制参数出现问题都走这里
            e.printStackTrace();
            mRecorder.reset();
            mRecorder.release();
            mRecorder  = null;
            showCameraPermission();
            FileUtils.deleteFile(viderFile.getPath());
            return false;
        }
        return true;
    }

    private String videoDir() {
        File sampleDir = new File(FileUtils.getAppPath());
        if (!sampleDir.exists()){
            sampleDir.mkdirs();
        }
        File vecordDir = sampleDir;
        //创建文件
        try {
            viderFile = File.createTempFile("recording","mp4",vecordDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化相机
     *
     * @param type    前后的类型
     * @param flashDo 赏光灯是否工作
     */
    private void initCamera(int type, boolean flashDo) {
        if (mCamera != null) {
            //如何已经初始化过，就先释放
            releaseCamera();
        }

        mCamera = Camera.open(type);
        if (mCamera == null) {
            showCameraPermission();
            return;
        }

        mCamera.lock();
        Camera.Parameters parameters = mCamera.getParameters();
        if (type == 0) {
            parameters.setPreviewSize(SIZE_1, SIZE_2);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//1、视频连续对焦
            mCamera.cancelAutoFocus();//2、如果要实现连续对焦，就要关闭自动对焦
        }
        mCamera.setParameters(parameters);
        FlashLogic(mCamera.getParameters(), flashType, flashDo);
        if (cameraType == 1) {
            frontCameraRotate();
            mCamera.setDisplayOrientation(frontRotate);
        } else {
            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            releaseCamera();
        }
        mCamera.startPreview();
        mCamera.unlock();
    }

    private void showCameraPermission() {
        Toast.makeText(this, "您没有开启相机权限或者录音权限", Toast.LENGTH_SHORT).show();
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int camreaCount = Camera.getNumberOfCameras();
        try {
            for (int i = 0; i < camreaCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
                if (cameraFlag == 1) {
                    //后置转前置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                        frontCameraRotate();//前置旋转摄像头度数
                        switchCameraLogic(i, 0, frontRotate);
                        break;
                    }
                } else {
                    //前置到后置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        switchCameraLogic(i, 1, 90);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * 处理摄像头切换逻辑
     *
     * @param i           哪一个，前置还是后置
     * @param flag        切换后的标志
     * @param orientation 旋转的角度
     */
    private void switchCameraLogic(int i, int flag, int orientation) {
        if (mCamera != null) {
            mCamera.lock();
        }
        endRecordUI();
        releaseCamera();
        mCamera = Camera.open(i);//打开当前选中的摄像头
        mCamera.setDisplayOrientation(orientation);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cameraFlag = flag;
        FlashLogic(mCamera.getParameters(), 0, false);
        mCamera.startPreview();
        cameraType = i;
        mCamera.unlock();
    }

    /**
     * 释放摄像头资源
     */
    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.lock();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制时候的状态
     */
    private void endRecordUI() {
        mSwicthCamera.setVisibility(View.VISIBLE);//旋转摄像头关闭
        mVideoFlashLight.setVisibility(View.VISIBLE);//闪光灯关闭
        mRecordButton.setImageResource(R.drawable.record);//录制按钮停止
    }

    /**
     * 旋转前置摄像头为正的
     */
    private void frontCameraRotate() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(1, info);
        int degrees = getDisplayRotation(this);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;// compensate the mirror
            result = (360 - result) % 360;
        } else {// back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        frontOri = info.orientation;
        frontRotate = result;
    }

    /**
     * 获取旋转角度
     */
    private int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    private void clickFlash() {
        if (mCamera == null) {
            return;
        }
        mCamera.lock();
        Camera.Parameters p = mCamera.getParameters();
        if (flashType == 0) {
            FlashLogic(p, 1, false);
        } else {
            FlashLogic(p, 0, false);
        }
        mCamera.unlock();
    }
}

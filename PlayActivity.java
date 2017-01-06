package com.jy.yisaidemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.dou361.ijkplayer.listener.OnShowThumbnailListener;
import com.dou361.ijkplayer.widget.PlayStateParams;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.shawnlin.preferencesmanager.PreferencesManager;
import com.zhy.autolayout.AutoLayoutActivity;

import java.util.HashMap;

import app.AppConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mycustomview.PlayerView;
import okhttp3.Call;
import okhttp3.Response;
import util.MediaUtil;


/**
 * Created by Administrator on 2016/12/30.
 */
public class PlayActivity extends AutoLayoutActivity {
    private static final String TAG = "PlayActivity";
    public final static String VIDEO_URL = "videoUrl";
    public final static String VIDEO_IMG_URL = "videoImgUrl";
    @BindView(R.id.btn_upload)
    Button mBtnUpload;
    AppConfig appConfig = AppConfig.getInstance();
    @BindView(R.id.btn_compress)
    Button mBtnCompress;

    private PlayerView player;
    private MediaMetadataRetriever mmr;
    private String videoUrl;
    private String videoImgUrl;
    private PowerManager.WakeLock wakeLock;

    private String mLoginkey;
    private String mUid;
    private View mMain;
    private LinearLayout mLinearLayout;

    private FFmpeg ffmpeg;
    private Bitmap mBitmap;


    private void initView() {
    }

    private void loadData() {
        ffmpeg = FFmpeg.getInstance(this);

        Intent intent = getIntent();
        videoUrl = intent.getStringExtra(VIDEO_URL);
        videoImgUrl = intent.getStringExtra(VIDEO_IMG_URL);

        mmr = new MediaMetadataRetriever();
        if (videoImgUrl == null){
            mmr.setDataSource(videoUrl);
            mBitmap = mmr.getFrameAtTime();
        }


        mMain = LayoutInflater.from(this).inflate(R.layout.player, null);
        hideVitualController(mMain);
        initSimpleVideoPlayer();
        mLoginkey = PreferencesManager.getString("loginkey");
        mUid = PreferencesManager.getString("uid");
        Log.d(TAG, "loadData: " + "loginkey" + mLoginkey + "uid" + mUid);
    }

    private void upload() {
        getToken();
    }

    private void getToken() {
        String url = appConfig.getApiUrl("qiNiu");
        HashMap<String, String> params = new HashMap<>();
        params.put("a", "getUpToken0");
        params.put("uid", mUid);
        params.put("loginkey", mLoginkey);
        OkGo.post(url)
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "onSuccess: " + s);
                    }
                });

    }

    //1.简单的播放器实现
    private void initSimpleVideoPlayer() {

        player = new PlayerView(this)
                .setTitle("录像")
                .setScaleType(PlayStateParams.fitparent)
                .hideMenu(true)
                .hideRotation(true)
                .hideSteam(true)
                .forbidTouch(false)
                .showThumbnail(new OnShowThumbnailListener() {
                    @Override
                    public void onShowThumbnail(ImageView ivThumbnail) {
                        Glide.with(PlayActivity.this)
                                .load(videoImgUrl)
                                .error(R.color.colorApp)
                                .placeholder(R.color.colorApp)
                                .into(ivThumbnail);
                    }
                })
                .setPlaySource(videoUrl);


    }

    /**
     * 虚拟按键的隐藏方法
     */
    private void hideVitualController(final View main) {
        main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                //比较Activity根布局与当前布局的大小
                int heightDiff = main.getRootView().getHeight() - main.getHeight();
                if (heightDiff > 100) {
                    //大小超过100时，一般为显示虚拟键盘事件
                    main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                } else {
                    //大小小于100时，为不显示虚拟键盘或虚拟键盘隐藏
                    main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                }
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (player != null) {
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        super.onBackPressed();
        /**demo的内容，恢复设备亮度状态*/
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

    @OnClick({R.id.btn_upload,R.id.btn_compress})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_upload:
                break;
            case R.id.btn_compress:
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_play);
        ButterKnife.bind(this);
        /**
         * 常亮
         */
        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveATG");

        initView();
        loadData();
        loadFFMpegBinary();
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler(){
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
            e.printStackTrace();
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Device not supported")
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PlayActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
        /**demo的内容，暂停系统其它媒体的状态*/
        MediaUtil.muteAudioFocus(this, false);
        /**demo的内容，激活设备常亮状态*/
        if (wakeLock != null) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
            mLinearLayout.setVisibility(View.GONE);
        }
        /**demo的内容，恢复系统其它媒体的状态*/
        MediaUtil.muteAudioFocus(this, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }
}

package com.jy.yisaidemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jy.jylibrary.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/12/28.
 */

public class MineActivity extends BaseActivity {

    @BindView(R.id.iv_header)
    ImageView mIvHeader;
    @BindView(R.id.message)
    Button mMessage;
    @BindView(R.id.works)
    Button mWorks;
    @BindView(R.id.collections)
    Button mCollections;
    @BindView(R.id.video)
    Button mVideo;
    @BindView(R.id.personalWorks)
    Button mPersonalWorks;
    @BindView(R.id.dynamics)
    Button mDynamics;
    @BindView(R.id.feedback)
    Button mFeedback;
    @BindView(R.id.share)
    Button mShare;
    @BindView(R.id.setting)
    Button mSetting;
    @BindView(R.id.softinput)
    Button mSoftinput;

    @Override
    protected int getContentId() {
        return R.layout.layout_mine;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_header, R.id.message, R.id.works, R.id.collections, R.id.video, R.id.personalWorks, R.id.dynamics, R.id.feedback, R.id.setting,R.id.softinput})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_header:
                break;
            case R.id.message:
                break;
            case R.id.works:
                break;
            case R.id.collections:
                break;
            case R.id.video:
                break;
            case R.id.personalWorks:
                break;
            case R.id.dynamics:
                break;
            case R.id.feedback:
                break;
            case R.id.setting:
                goToActivity(SettingActivity.class);
                break;
            case R.id.softinput:
                goToActivity(SoftInputActivity.class);
        }
    }

    //跳转方法
    public void goToActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }
}

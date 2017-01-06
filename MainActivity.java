package com.jy.yisaidemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jy.jylibrary.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.shouye)
    Button mShouye;
    @BindView(R.id.bisai)
    Button mBisai;
    @BindView(R.id.paishe)
    Button mPaishe;
    @BindView(R.id.faxian)
    Button mFaxian;
    @BindView(R.id.wode)
    Button mWode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_main;
    }

    @OnClick({R.id.shouye, R.id.bisai, R.id.paishe, R.id.faxian, R.id.wode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shouye:
                goToActivity(HomeActivity.class);
                break;
            case R.id.bisai:
                goToActivity(CompetitonActivity.class);
                break;
            case R.id.paishe:
                //goToActivity(RecordActivity.class);
                goToActivity(RecordActivity.class);
                break;
            case R.id.faxian:
                goToActivity(DiscoveryActivity.class);
                break;
            case R.id.wode:
                goToActivity(MineActivity.class);
                break;
        }
    }


    public void goToActivity(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
    }
}

package com.jy.yisaidemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.utils.CleanUtils;
import com.jy.jylibrary.base.BaseActivity;
import com.sdsmdg.tastytoast.TastyToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/12/28.
 */

public class SettingActivity extends BaseActivity {
    private static final String TAG = "SettingActivity";
    @BindView(R.id.clearCache)
    Button mClearCache;
    @BindView(R.id.clearFiles)
    Button mClearFiles;
    @BindView(R.id.hideComment)
    Button mHideComment;
    @BindView(R.id.aboutApp)
    Button mAboutApp;
    @BindView(R.id.loginOut)
    Button mLoginOut;
    @BindView(R.id.loginIn)
    Button mLoginIn;
    @BindView(R.id.register)
    Button mRegister;

    @Override
    protected int getContentId() {
        return R.layout.layout_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.clearCache, R.id.clearFiles, R.id.hideComment, R.id.aboutApp, R.id.loginOut, R.id.loginIn,R.id.register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clearCache:
                //清理内部缓存
                CleanUtils.cleanInternalCache();
                //清理外部缓存
                CleanUtils.cleanExternalCache();
                TastyToast.makeText(this, "清理缓存成功", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                break;
            case R.id.clearFiles:
                //清理内部文件
                CleanUtils.cleanInternalFiles();
                //清除自定义目录下的文件
                /*String path = Environment.getExternalStorageState();
                CleanUtils.cleanCustomCache(path);*/
                TastyToast.makeText(this, "清理文件成功", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                break;
            case R.id.hideComment:
                break;
            case R.id.aboutApp:
                break;
            case R.id.register:
                Log.d(TAG, "onClick: ");
                goToActivity(RoleSelectActivity.class);
                break;
            case R.id.loginIn:
                goToActivity(LoginActivity.class);
                break;
            case R.id.loginOut:
                break;
        }
    }


    public void goToActivity(Class c){
        Intent intent = new Intent(this,c);
        startActivity(intent);
    }
}

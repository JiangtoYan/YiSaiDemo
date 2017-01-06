package com.jy.yisaidemo;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jy.jylibrary.base.BaseActivity;
import com.shawnlin.preferencesmanager.PreferencesManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/12/29.
 */
public class RoleSelectActivity extends BaseActivity {
    @BindView(R.id.rg_student)
    RadioButton mRgStudent;
    @BindView(R.id.rg_teacher)
    RadioButton mRgTeacher;
    @BindView(R.id.rg_select)
    RadioGroup mRgSelect;

    @Override
    protected int getContentId() {
        return R.layout.layout_roleselect;
    }


    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void loadDatas() {
        super.loadDatas();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.rg_student, R.id.rg_teacher})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rg_student:
                PreferencesManager.putInt("selected_role",0);
                gotoActivity(RegisterActivity.class,true);
                break;
            case R.id.rg_teacher:
                PreferencesManager.putInt("selected_role",1);
                gotoActivity(RegisterActivity.class,true);
                break;
        }
    }
}

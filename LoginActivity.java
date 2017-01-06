package com.jy.yisaidemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.utils.StringUtils;
import com.jy.jylibrary.base.BaseActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.shawnlin.preferencesmanager.PreferencesManager;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/29.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.btn_login)
    Button mBtnLogin;
    private String phone;
    private String password;
    private AppConfig appConfig = AppConfig.getInstance();

    private String url;

    @Override
    protected int getContentId() {
        return R.layout.layout_login;
    }

    @Override
    protected void init() {
        super.init();
        phone = PreferencesManager.getString("phone");
        password = PreferencesManager.getString("password");
        Log.d(TAG, "init: " + "phone" + phone + "password" + password);
        if (!StringUtils.isEmpty(phone) && !StringUtils.isEmpty(password)) {
            mEtPhone.setText(phone);
            mEtPassword.setText(password);
        }


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

    @OnClick(R.id.btn_login)
    public void onClick() {
        url = appConfig.getApiUrl("user_login");
        phone = mEtPhone.getText().toString();
        password = mEtPassword.getText().toString();
        Log.d(TAG, "onClick: " + "phone" + phone + "password" + password);
        Log.d(TAG, "onClick: " + url);
        if (!StringUtils.isEmpty(phone) && !StringUtils.isEmpty(password)) {
            PreferencesManager.putString("phone", phone);
            PreferencesManager.putString("password", password);
            doLoginIn(phone, password);
        }
    }

    /**
     * 登录
     *
     * @param phone
     * @param password
     */
    private void doLoginIn(String phone, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("a", "withPhone");
        params.put("phone", phone);
        params.put("passwd", password);
        OkGo.post(url)
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "onSuccess: " + s);
                        try {
                            JSONObject result = new JSONObject(s);
                            String code = result.getString("code");
                            Log.d(TAG, "onSuccess: " + code);

                            //登录成功后再保存一遍用户信息
                            if (checkGetResult(code)) {

                                JSONObject userData = result.getJSONObject("data");
                                String loginkey = userData.getString("loginkey");
                                String role_type = userData.getString("role_type");
                                String uid = userData.getString("uid");

                                PreferencesManager.putString("loginkey", loginkey);
                                PreferencesManager.putString("role_type", role_type);
                                PreferencesManager.putString("uid", uid);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                });

    }

    /**
     * 检查登录返回码
     *
     * @param code
     * @return
     */
    private boolean checkGetResult(String code) {

        if (code != null) {
            switch (code) {

                case "1":
                    Toast.makeText(ApplicationContext, "登录成功", Toast.LENGTH_SHORT).show();
                    return true;

                case "1001":
                    Toast.makeText(ApplicationContext, "请填写手机号码", Toast.LENGTH_SHORT).show();
                    return false;

                case "1002":
                    Toast.makeText(ApplicationContext, "不是有效的手机号码", Toast.LENGTH_SHORT).show();
                    return false;

                case "1003":
                    Toast.makeText(ApplicationContext, "手机号码未注册", Toast.LENGTH_SHORT).show();
                    return false;

                case "2002":
                    Toast.makeText(ApplicationContext, "密码不正确", Toast.LENGTH_SHORT).show();
                    return false;

                case "9000":
                case "9001":
                case "9002":
                    Toast.makeText(ApplicationContext, "请求格式错误", Toast.LENGTH_SHORT).show();
                    return false;

                case "-1":

                    Map<String, String> mapLogin = new HashMap<>();
                    mapLogin.put("role", "无身份辨识");
                    mapLogin.put("result", "failure");
                    MobclickAgent.onEvent(mContext, "login", mapLogin);

                    Toast.makeText(ApplicationContext, "获取数据失败", Toast.LENGTH_SHORT).show();
                    return false;

                default:

                    Toast.makeText(ApplicationContext, "默认处理", Toast.LENGTH_SHORT).show();
                    return false;
            }

        } else {
            Toast.makeText(ApplicationContext, "数据返回异常", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

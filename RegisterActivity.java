package com.jy.yisaidemo;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.utils.RegexUtils;
import com.blankj.utilcode.utils.StringUtils;
import com.jy.jylibrary.base.BaseActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.sdsmdg.tastytoast.TastyToast;
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
import mycustomview.TimeButton;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/28.
 */
public class RegisterActivity extends BaseActivity {
    @BindView(R.id.btn_getVcode)
    TimeButton mBtnGetVcode;
    @BindView(R.id.btn_register)
    Button mBtnRegister;
    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_password)
    EditText mEtPassword;
    @BindView(R.id.et_vode)
    EditText mEtVode;

    private static final String TAG = "RegisterActivity";
    private AppConfig appConfig = AppConfig.getInstance();
    private String phone;
    private String password;
    private String vCode;
    private ProgressDialog mProgressDialog;
    private String mUrl;


    @Override
    protected int getContentId() {
        return R.layout.layout_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    protected void init() {
        super.init();
        mBtnGetVcode.setBeforeColor(Color.BLUE);
        mBtnGetVcode.setAfterColor(Color.RED);


        int selected_role = PreferencesManager.getInt("selected_role");
        Log.d(TAG, "init: " + selected_role);
        switch (selected_role){
            case 0:
                mUrl =appConfig.getApiUrl("user_register");
                break;
            case 1:
            case 2:
                mUrl = appConfig.getApiUrl("judge_teacher_register");
                break;
        }
        Log.d(TAG, "init: " + mUrl);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @OnClick({R.id.btn_getVcode, R.id.btn_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_getVcode:
                if (checkPhone()) {
                    getVode();
                }
                break;
            case R.id.btn_register:
                if (checkSubmit()){
                    registerSubmit();
                }
                break;

        }
    }

    /**
     * 检验手机号
     * @return
     */
    private boolean checkPhone() {
        phone = mEtPhone.getText().toString();
        if (StringUtils.isEmpty(phone)) {
            TastyToast.makeText(this, "请填写手机号码", TastyToast.LENGTH_SHORT, TastyToast.INFO);
            return false;
        }
        if (!RegexUtils.isMobileSimple(phone)) {
            TastyToast.makeText(this, "请填写正确的手机号码", TastyToast.LENGTH_SHORT, TastyToast.INFO);
        }
        return true;
    }

    /**
     * 检验密码
     * @return
     */
    private boolean checkPassword(){
        password = mEtPassword.getText().toString();

        if (StringUtils.isEmpty(password)) {
            Toast.makeText(this, "请填写密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "密码长度大于或等于8位", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() > 16) {
            Toast.makeText(this, "密码长度小于或等于16位", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 检验验证码
     * @return
     */
    private boolean checkVcode(){
        vCode = mEtVode.getText().toString();

        if (StringUtils.isEmpty(vCode)) {
            Toast.makeText(this, "请填写验证码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (vCode.length() != 4) {
            Toast.makeText(this, "请输入正确的验证码", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 获取验证码
     */
    private void getVode() {
        mProgressDialog = ProgressDialog.show(this, "获取验证码", "请求发送中...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        HashMap<String, String> params = new HashMap<>();
        params.put("a", "vcode");
        params.put("phone", phone);
        Log.d(TAG, "getVode: " + mUrl);
        OkGo.post(mUrl)
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "onSuccess: " + s);
                        mProgressDialog.dismiss();
                        try {
                            JSONObject jo = new JSONObject(s);
                            String code = jo.getString("code");
                            checkVcodeResult(code);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        mProgressDialog.dismiss();
                    }
                });
    }

    /**
     * 检验获取验证码信息
     * @param code
     * @return
     */
    private boolean checkVcodeResult(String code) {
        if (code != null) {
            switch (code) {
                case "1":
                    showTasty("验证码已发送，请注意查收",TastyToast.INFO);
                    mBtnGetVcode.startCountdown();
                    return true;
                case "9000":
                case "9001":
                case "6001":
                    showTasty("请求格式错误",TastyToast.ERROR);
                    break;
                case "1001":
                case "1002":
                    showTasty("请填写正确的手机号码",TastyToast.ERROR);
                    break;
                case "1003":
                    showTasty("该手机号码已被使用，请尝试其他手机号码",TastyToast.ERROR);
                    break;
                case "1004":
                    showTasty("该手机号码已被使用，请尝试其他手机号码",TastyToast.ERROR);
                    break;
                case "2001":
                case "2002":
                case "2003":
                    showTasty("获取验证码失败",TastyToast.ERROR);
                    break;
                case "4001":
                case "4002":
                case "4003":
                case "4004":
                case "4005":
                case "4006":
                case "4007":
                case "4008":
                    showTasty("验证码发送失败",TastyToast.ERROR);
                    break;
                case "5004":
                    showTasty("24小时只能获取10次验证码",TastyToast.ERROR);
                    break;
                case "5002":
                    showTasty("一小时只能获取三次验证码，请稍后重试",TastyToast.ERROR);
                    break;
                case "5003":
                case "5001":
                    showTasty("验证码发送间隔不小于1分钟，请稍后重试",TastyToast.ERROR);
                    break;
                case "0":
                case "-1":
                    showTasty("获取数据失败",TastyToast.ERROR);
                    break;
                default:
                    showTasty("默认处理",TastyToast.DEFAULT);
                    break;
            }
            return false;
        }
        return true;
    }

    /**
     * 检验登录
     * @return
     */
    private boolean checkSubmit() {
        checkPhone();
        checkPassword();
        checkVcode();
        return true;
    }

    /**
     * 注册
     */
    private void registerSubmit(){
        mProgressDialog = ProgressDialog.show(this, "提交注册", "注册中……");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        int roleType = PreferencesManager.getInt("selected_role");
        Log.d(TAG, "registerSubmit: " + roleType);
        switch (roleType){
            case 0://注册学生
                doRegister(phone,password,vCode,"");
                break;
            case 1://注册老师
                doRegister(phone,password,vCode,"");
                break;
        }
    }

    /**
     * 老师注册Http请求
     * @param phone
     * @param passwd
     * @param vcode
     * @param openid
     */
    protected void doRegister(String phone, String passwd, String vcode, String openid){
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("username", "");
        paramsMap.put("phone", phone);
        paramsMap.put("passwd", passwd);
        paramsMap.put("vcode", vcode);
        paramsMap.put("a", "phone");
        paramsMap.put("filename", "");
        paramsMap.put("openid", openid);
        OkGo.post(mUrl)
                .params(paramsMap)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        mProgressDialog.dismiss();
                        if (!StringUtils.isEmpty(s)){
                        Log.d(TAG, "onSuccess: " + s);
                            putUserIntoCache(s);
                        }else {
                            showTasty("相应数据为空",TastyToast.ERROR);
                        }
                    }
                });
    }


    /**
     * 将用户存到偏好储存
     * @param msg
     */
    public void putUserIntoCache(String msg){
        try {
            JSONObject result = new JSONObject(msg);
            String code = result.getString("code");
            if (checkRegisterResult(code)) {
                JSONObject userData = result.getJSONObject("data");

                String loginkey = userData.getString("loginkey");
                String role_type = userData.getString("role_type");
                String uid = userData.getString("uid");

                //注册成功后保存一遍用户信息
                PreferencesManager.putString("loginkey", loginkey);
                PreferencesManager.putString("role_type", role_type);
                PreferencesManager.putString("uid", uid);

                PreferencesManager.putString("phone", phone);
                PreferencesManager.putString("password", password);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查注册请求返回码
     * @param code
     * @return
     */
    private boolean checkRegisterResult(String code) {

        if (code != null) {
            switch (code) {
                case "1":
                    Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                    this.finish();
                    return true;
                case "9000":
                case "9001":
                case "6001":
                    Toast.makeText(this, "请填写手机号码", Toast.LENGTH_SHORT).show();
                    break;
                case "1001":
                    Toast.makeText(this, "请填写正确的手机号码", Toast.LENGTH_SHORT).show();
                    break;
                case "1002":
                    Toast.makeText(this, "身份信息过期，请重新登录", Toast.LENGTH_SHORT).show();
                    break;
                case "1003":
                    Toast.makeText(this, "该手机号码已注册过，请尝试其他手机号码", Toast.LENGTH_SHORT).show();
                    break;
                case "1004":
                    Toast.makeText(this, "该手机号码已被使用，请尝试其他手机号码", Toast.LENGTH_SHORT).show();
                    break;
                case "2001":
                    Toast.makeText(this, "请填写密码", Toast.LENGTH_SHORT).show();
                    break;
                case "2003":
                    Toast.makeText(this, "密码的长度为8-16位字符", Toast.LENGTH_SHORT).show();
                    break;
                case "3001":
                    Toast.makeText(this, "请填写验证码", Toast.LENGTH_SHORT).show();
                    break;
                case "3002":
                    Toast.makeText(this, "验证码有误", Toast.LENGTH_SHORT).show();
                    break;
                case "4001":
                    Toast.makeText(this, "请选择头像图片", Toast.LENGTH_SHORT).show();
                    break;
                case "5001":
                    Toast.makeText(this, "请填写昵称", Toast.LENGTH_SHORT).show();
                    break;
                case "-1":
                    Map<String, String> mapRegister = new HashMap<>();
                    mapRegister.put("role", "无身份辨识");
                    mapRegister.put("result", "failure");
                    MobclickAgent.onEvent(this, "register", mapRegister);
                    Toast.makeText(this, "获取数据失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "默认处理", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }

        return true;
    }

}

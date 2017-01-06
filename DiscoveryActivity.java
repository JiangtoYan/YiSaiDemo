package com.jy.yisaidemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jy.jylibrary.base.BaseActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import adapter.VideoListRecycleViewAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import entity.Video;
import okhttp3.Call;
import okhttp3.Response;


/**
 * Created by Administrator on 2016/12/28.
 */

public class DiscoveryActivity extends BaseActivity{

    private static final String TAG = "DiscoveryActivity";
    public String url = "http://120.25.237.16:8011/findModule";


    @BindView(R.id.XRecyclerView)
    XRecyclerView mXRecyclerView;
    private VideoListRecycleViewAdapter mVideoListAdapter;
    private List<Video> mVideoList;
    private int start = 0;
    private int num = 10;


    @Override
    protected int getContentId() {
        return R.layout.layout_discovery;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        refreshData();
    }


    @Override
    protected void initView() {
        super.initView();

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,2);
        mXRecyclerView.setLayoutManager(mGridLayoutManager);
        mXRecyclerView.setRefreshProgressStyle(ProgressStyle.Pacman);
        mXRecyclerView.setLoadingMoreProgressStyle(ProgressStyle.Pacman);
        mVideoListAdapter = new VideoListRecycleViewAdapter(this);
        mXRecyclerView.setAdapter(mVideoListAdapter);
        mXRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                start = 0;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                        mXRecyclerView.refreshComplete();
                    }
                },1000);
            }

            @Override
            public void onLoadMore() {
                start += 10;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                        mXRecyclerView.loadMoreComplete();
                    }
                },1000);
            }
        });
    }

    @Override
    protected void loadDatas() {
        super.loadDatas();
    }


    private void refreshData(){
        HashMap<String,String> params = new HashMap<>();
        params.put("a","findModuleData");
        params.put("start", String.valueOf(start));
        params.put("num", String.valueOf(num));
        OkGo.post(url)
                .params(params)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "onSuccess: " + s);
                        try {
                            JSONObject jo = new JSONObject(s);
                            JSONArray ja = jo.getJSONObject("data").getJSONArray("lst_findwork");
                            TypeToken<List<Video>> tt = new TypeToken<List<Video>>(){};
                            mVideoList = new Gson().fromJson(ja.toString(),tt.getType());
                            if (mVideoList.size() == 0){
                                mXRecyclerView.setNoMore(true);
                                mVideoListAdapter.notifyDataSetChanged();
                            }
                            if (start == 0){
                                mVideoListAdapter.removeAllData();
                            }
                            mVideoListAdapter.addDatas(mVideoList);
                            Log.d(TAG, "onSuccess: " + mVideoList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}

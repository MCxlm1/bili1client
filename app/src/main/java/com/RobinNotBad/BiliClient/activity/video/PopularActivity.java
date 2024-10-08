package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//热门页面
//2024-01-14

public class PopularActivity extends InstanceActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;
    private boolean refreshing = false;

    private int page = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick(1);
        Log.e("debug","进入热门页");

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadPopular);

        TextView title = findViewById(R.id.pageName);
        title.setText("热门");

        loadPopular();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadPopular() {
        Log.e("debug", "刷新");
        page = 1;
        if (firstRefresh) {
            recyclerView.setLayoutManager(new LinearLayoutManager(PopularActivity.this));
            videoCardList = new ArrayList<>();
        } else {
            int last = videoCardList.size();
            videoCardList.clear();
            videoCardAdapter.notifyItemRangeRemoved(0,last);
        }
        swipeRefreshLayout.setRefreshing(true);

        refreshing = true;
        CenterThreadPool.run(this::addPopular);
    }

    private void addPopular() {
        Log.e("debug", "加载下一页");
        runOnUiThread(()->swipeRefreshLayout.setRefreshing(true));
        int lastSize = videoCardList.size();
        try {
            RecommendApi.getPopular(videoCardList,page);
            page++;
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;
                if (firstRefresh) {
                    firstRefresh = false;
                    videoCardAdapter = new VideoCardAdapter(this, videoCardList);
                    recyclerView.setAdapter(videoCardAdapter);

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                        }
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            assert manager != null;
                            int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                            int itemCount = manager.getItemCount();
                            if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing) {// 滑动到倒数第三个就可以刷新了
                                refreshing = true;
                                CenterThreadPool.run(()->addPopular()); //加载第二页
                            }
                        }
                    });
                }else {
                    Log.e("debug","last="+lastSize+"&now="+videoCardList.size());
                    videoCardAdapter.notifyItemRangeInserted(lastSize,videoCardList.size()-lastSize);
                }
            });
        } catch (Exception e){runOnUiThread(()-> MsgUtil.err(e,this));}
    }
}
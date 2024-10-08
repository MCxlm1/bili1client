package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;

import java.util.Timer;
import java.util.TimerTask;

public class DialogActivity extends BaseActivity {

    int wait_time = 0;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        MaterialButton close_btn = findViewById(R.id.close_btn);

        Intent intent = getIntent();

        ((TextView)findViewById(R.id.tip_title)).setText(intent.getStringExtra("title"));
        ((TextView)findViewById(R.id.content)).setText(intent.getStringExtra("content"));
        if(intent.getIntExtra("img_id",-1) != -1){
            ImageFilterView imageFilterView = findViewById(R.id.image);
            Glide.with(this).load(intent.getIntExtra("img_id",R.mipmap.placeholder))
                    .override(Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageFilterView);
        }
        else if(intent.getStringExtra("img_id") != null){
            ImageFilterView imageFilterView = findViewById(R.id.image);
            Glide.with(this).load(intent.getStringExtra("img_id"))
                    .override(Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageFilterView);
        }

        if(intent.getBooleanExtra("wait",false)){
            findViewById(R.id.close_btn).setEnabled(false);
            wait_time = intent.getIntExtra("wait_time",3);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        if(wait_time > 0){
                            close_btn.setText("知道了(" + wait_time + "s)");
                            findViewById(R.id.close_btn).setEnabled(false);
                            wait_time--;
                        }else{
                            close_btn.setText("知道了");
                            findViewById(R.id.close_btn).setEnabled(true);
                            timer.cancel();
                        }
                    });
                }
            },0,1000);
        }else close_btn.setEnabled(true);
        close_btn.setOnClickListener(view -> finish());
    }

    @Override
    public void onBackPressed() {
    }
}
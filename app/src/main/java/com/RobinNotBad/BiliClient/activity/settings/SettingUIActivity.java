package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Switch;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

public class SettingUIActivity extends BaseActivity {

    private EditText uiScaleInput,uiPaddingH,uiPaddingV;

    @SuppressLint({"UseSwitchCompatOrMaterialCode", "MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_ui);
        Log.e("debug","进入界面设置");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        uiScaleInput = findViewById(R.id.ui_scale_input);
        uiScaleInput.setText(String.valueOf(SharedPreferencesUtil.getFloat("dpi",1.0F)));

        uiPaddingH = findViewById(R.id.ui_padding_horizontal);
        uiPaddingH.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingH_percent",0)));
        uiPaddingV = findViewById(R.id.ui_padding_vertical);
        uiPaddingV.setText(String.valueOf(SharedPreferencesUtil.getInt("paddingV_percent",0)));


        MaterialCardView preview = findViewById(R.id.preview);
        preview.setOnClickListener(view -> {
            save();
            Intent intent = new Intent();
            intent.setClass(SettingUIActivity.this, UIPreviewActivity.class);
            startActivity(intent);
        });
    }

    private void save() {
        if(!uiScaleInput.getText().toString().equals("")) {
            float dpiTimes = Float.parseFloat(uiScaleInput.getText().toString());
            if (dpiTimes >= 0.25F && dpiTimes <= 5.0F)
                SharedPreferencesUtil.putFloat("dpi", dpiTimes);
            Log.e("dpi", uiScaleInput.getText().toString());
        }

        if(!uiPaddingH.getText().toString().equals("")) {
            int paddingH = Integer.parseInt(uiPaddingH.getText().toString());
            if(paddingH <= 30) SharedPreferencesUtil.putInt("paddingH_percent", paddingH);
            Log.e("paddingH", uiPaddingH.getText().toString());
        }

        if(!uiPaddingV.getText().toString().equals("")) {
            int paddingV = Integer.parseInt(uiPaddingV.getText().toString());
            if(paddingV <= 30) SharedPreferencesUtil.putInt("paddingV_percent", paddingV);
            Log.e("paddingV", uiPaddingV.getText().toString());
        }

    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}
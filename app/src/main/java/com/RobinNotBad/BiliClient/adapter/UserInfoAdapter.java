package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.message.PrivateMsgActivity;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

//用户信息页专用Adapter 独立出来也是为了做首项不同

public class UserInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Dynamic> dynamicList;
    UserInfo userInfo;
    boolean desc_expand, notice_expand;

    boolean follow_onprocess;

    public UserInfoAdapter(Context context, ArrayList<Dynamic> dynamicList, UserInfo userInfo) {
        this.context = context;
        this.dynamicList = dynamicList;
        this.userInfo = userInfo;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_user_info, parent, false);
            return new UserInfoHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_dynamic, parent, false);
            return new DynamicHolder(view,false);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DynamicHolder) {
            int realPosition = position - 1;
            DynamicHolder dynamicHolder = (DynamicHolder) holder;

            dynamicHolder.showDynamic(dynamicList.get(realPosition), context, true);

            if(dynamicList.get(realPosition).dynamic_forward != null){
                View childCard = View.inflate(context,R.layout.cell_dynamic_child,dynamicHolder.extraCard);
                DynamicHolder childHolder = new DynamicHolder(childCard,true);
                childHolder.showDynamic(dynamicList.get(realPosition).dynamic_forward, context, true);
            }
        }
        if (holder instanceof UserInfoHolder){
            UserInfoHolder userInfoHolder = (UserInfoHolder) holder;
            userInfoHolder.userName.setText(userInfo.name);
            userInfoHolder.userDesc.setText(userInfo.sign);
            if (!userInfo.notice.isEmpty()) userInfoHolder.userNotice.setText(userInfo.notice);
            else userInfoHolder.userNotice.setVisibility(View.GONE);
            userInfoHolder.userFans.setText("Lv" + userInfo.level + "\n" + ToolsUtil.toWan(userInfo.fans) + "粉丝");

            if(userInfo.official != 0) {
                userInfoHolder.officialIcon.setVisibility(View.VISIBLE);
                userInfoHolder.userOfficial.setVisibility(View.VISIBLE);
                String[] official_signs = {"哔哩哔哩不知名UP主","哔哩哔哩知名UP主","哔哩哔哩大V达人","哔哩哔哩企业认证",
                        "哔哩哔哩组织认证","哔哩哔哩媒体认证","哔哩哔哩政府认证","哔哩哔哩高能主播","社会知名人士"};
                userInfoHolder.userOfficial.setText(official_signs[userInfo.official] + (userInfo.officialDesc.isEmpty() ? "" : ("\n" + userInfo.officialDesc)));
            } else {
                userInfoHolder.officialIcon.setVisibility(View.GONE);
                userInfoHolder.userOfficial.setVisibility(View.GONE);
            }

            Glide.with(this.context).load(userInfo.avatar + "@20q.webp")
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(userInfoHolder.userAvatar);

            userInfoHolder.userAvatar.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, ImageViewerActivity.class);
                ArrayList<String> imageList = new ArrayList<>();
                imageList.add(userInfo.avatar);
                intent.putExtra("imageList", imageList);
                context.startActivity(intent);
            });

            if((userInfo.mid == SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)) || (userInfo.mid == 0)) userInfoHolder.followBtn.setVisibility(View.GONE);
            else userInfoHolder.followBtn.setChecked(userInfo.followed);
            userInfoHolder.followBtn.setOnClickListener(btn -> {
                if(!follow_onprocess) {
                    follow_onprocess = true;
                    userInfoHolder.setFollowed(!(userInfo.followed));
                    CenterThreadPool.run(() -> {
                        try {
                            int result = UserInfoApi.followUser(userInfo.mid,!(userInfo.followed));
                            if(result == 0){
                                userInfo.followed = !(userInfo.followed);
                                CenterThreadPool.runOnUiThread(()->MsgUtil.toast("操作成功",context));
                            } else {
                                userInfoHolder.setFollowed(userInfo.followed);
                                CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("操作失败："+result,context));
                            }
                        } catch (Exception e) {
                            CenterThreadPool.runOnUiThread(()->MsgUtil.err(e,context));
                        }
                        follow_onprocess = false;
                    });
                }
            });

            userInfoHolder.setFollowed(userInfo.followed);

            userInfoHolder.msgBtn.setOnClickListener(view -> {
                Intent intent = new Intent(context, PrivateMsgActivity.class);
                intent.putExtra("uid",userInfo.mid);
                context.startActivity(intent);
            });

            userInfoHolder.userDesc.setOnClickListener(view1 -> {
                if (desc_expand) userInfoHolder.userDesc.setMaxLines(2);
                else userInfoHolder.userDesc.setMaxLines(32);
                desc_expand = !desc_expand;
            });

            userInfoHolder.userNotice.setOnClickListener(view1 -> {
                if (notice_expand) userInfoHolder.userNotice.setMaxLines(2);
                else userInfoHolder.userNotice.setMaxLines(32);
                notice_expand = !notice_expand;
            });
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof DynamicHolder) ((DynamicHolder)holder).extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return dynamicList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position==0 ? 0 : 1);
    }

    public static class UserInfoHolder extends RecyclerView.ViewHolder{
        TextView userName,userFans,userDesc,userNotice,userOfficial;
        ImageView userAvatar,officialIcon;

        MaterialButton followBtn,msgBtn;

        public UserInfoHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userDesc = itemView.findViewById(R.id.userDesc);
            userNotice = itemView.findViewById(R.id.userNotice);
            userFans = itemView.findViewById(R.id.userFans);
            userOfficial = itemView.findViewById(R.id.userOfficial);
            officialIcon = itemView.findViewById(R.id.officialIcon);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            followBtn = itemView.findViewById(R.id.followBtn);
            msgBtn = itemView.findViewById(R.id.msgBtn);
        }

        public void setFollowed(boolean followed){
            msgBtn.setVisibility((followed ? View.VISIBLE : View.GONE));
            followBtn.setBackgroundTintList(ColorStateList.valueOf((followed ? Color.argb(0xDD,0x26,0x26,0x26) : Color.argb(0xFE,0xF0,0x5D,0x8E))));
            followBtn.setText((followed ? "已关注" : "关注"));
        }
    }
}

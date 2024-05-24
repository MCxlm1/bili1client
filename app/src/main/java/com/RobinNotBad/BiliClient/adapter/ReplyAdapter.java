package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.ReplyInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.WriteReplyActivity;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.RobinNotBad.BiliClient.view.CustomListView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

//评论Adapter
//2023-07-22

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Reply> replyList;
    long oid, root;
    int type, sort;
    public boolean isDynamic;
    OnItemClickListener listener;

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort, boolean isDynamic) {
        this.context = context;
        this.replyList = replyList;
        this.oid = oid;
        this.root = root;
        this.type = type;
        this.sort = sort;
        this.isDynamic = isDynamic;
    }

    public ReplyAdapter(Context context, ArrayList<Reply> replyList, long oid, long root, int type, int sort) {
        this(context, replyList, oid, root, type, sort, false);
    }

    public void setOnSortSwitchListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_action,parent,false);
            return new WriteReply(view);
        }
        else{
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_reply_list,parent,false);
            return new ReplyHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof WriteReply){
            WriteReply writeReply = (WriteReply) holder;
            writeReply.write_reply.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid",oid);
                intent.putExtra("rpid",root);
                intent.putExtra("parent",root);
                intent.putExtra("parentSender","");
                intent.putExtra("isDynamic", isDynamic);
                context.startActivity(intent);
            });
            String[] sorts = {"时间排序","点赞排序","回复排序"};
            writeReply.sort.setText(sorts[sort]);
            writeReply.sort.setOnClickListener(view -> {
                if(this.listener!=null) listener.onItemClick(0);
            });
        }
        if(holder instanceof ReplyHolder) {
            int realPosition = position - 1;
            ReplyHolder replyHolder = (ReplyHolder) holder;

            Glide.with(context).load(GlideUtil.url(replyList.get(realPosition).sender.avatar))
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(replyHolder.replyAvatar);
            replyHolder.userName.setText(replyList.get(realPosition).sender.name);


            String text = replyList.get(realPosition).message;
            replyHolder.message.setText(text);  //防止加载速度慢时露出鸡脚
            ToolsUtil.setCopy(replyHolder.message,context);
            if(replyList.get(realPosition).emote != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(text, replyList.get(realPosition).emote, 1.0f, context);
                        ((Activity) context).runOnUiThread(() -> replyHolder.message.setText(spannableString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

            replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount));

            if (replyList.get(realPosition).liked){           //这里，还有下面，一定要加else！否则会导致错乱
                replyHolder.likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
            } else {
                replyHolder.likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
            }

            if (replyList.get(realPosition).childCount != 0) {
                replyHolder.childReplyCard.setVisibility(View.VISIBLE);

                if (replyList.get(realPosition).upReplied) replyHolder.childCount.setText("UP主在内 共" + replyList.get(realPosition).childCount + "条回复");
                else replyHolder.childCount.setText("共" + replyList.get(realPosition).childCount + "条回复");

                if(replyList.get(realPosition).childMsgList != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.cell_reply_child, replyList.get(realPosition).childMsgList);
                    replyHolder.childReplies.setAdapter(adapter);
                }
            } else replyHolder.childReplyCard.setVisibility(View.GONE);

            if (replyList.get(realPosition).upLiked) replyHolder.upLiked.setVisibility(View.VISIBLE);
            else replyHolder.upLiked.setVisibility(View.GONE);
            replyHolder.pubDate.setText(replyList.get(realPosition).pubTime);

            if (replyList.get(realPosition).pictureList != null && !replyList.get(realPosition).pictureList.isEmpty()) {  //图片显示相关
                replyHolder.imageCard.setVisibility(View.VISIBLE);
                replyHolder.imageCount.setVisibility(View.VISIBLE);
                Glide.with(context).load(GlideUtil.url(replyList.get(realPosition).pictureList.get(0)))
                        .placeholder(R.mipmap.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(replyHolder.imageCard);

                replyHolder.imageCount.setText("共" + replyList.get(realPosition).pictureList.size() + "张图片");
                replyHolder.imageCard.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(context, ImageViewerActivity.class);
                    intent.putExtra("imageList", replyList.get(realPosition).pictureList);
                    context.startActivity(intent);
                });
            } else {
                replyHolder.imageCount.setVisibility(View.GONE);
                replyHolder.imageCard.setVisibility(View.GONE);
            }

            replyHolder.childReplyCard.setOnClickListener(view -> {
                startReplyInfoActivity(replyList.get(realPosition).rpid, oid, type);
            });
            replyHolder.childReplies.setOnItemClickListener((adapterView, view, i, l) -> {
                startReplyInfoActivity(replyList.get(realPosition).rpid, oid, type);
            });

            replyHolder.replyAvatar.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, UserInfoActivity.class);
                intent.putExtra("mid", replyList.get(realPosition).sender.mid);
                context.startActivity(intent);
            });

            replyHolder.likeCount.setOnClickListener(view -> CenterThreadPool.run(() -> {
                if (!replyList.get(realPosition).liked) {
                    try {
                        if (ReplyApi.likeReply(oid, replyList.get(realPosition).rpid, true) == 0) {
                            replyList.get(realPosition).liked = true;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("点赞成功",context);
                                replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount + 1));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xfe,0x67,0x9a));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_liked),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("点赞失败",context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (ReplyApi.likeReply(oid, replyList.get(realPosition).rpid, false) == 0) {
                            replyList.get(realPosition).liked = false;
                            ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("取消成功",context);
                                replyHolder.likeCount.setText(String.valueOf(replyList.get(realPosition).likeCount));
                                replyHolder.likeCount.setTextColor(Color.rgb(0xff,0xff,0xff));
                                replyHolder.likeCount.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context,R.drawable.icon_like),null,null,null);
                            });
                        } else
                            ((Activity) context).runOnUiThread(() -> MsgUtil.toast("取消失败",context));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }));

            replyHolder.replyBtn.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(context, WriteReplyActivity.class);
                intent.putExtra("oid",oid);
                intent.putExtra("rpid",replyList.get(realPosition).rpid);
                intent.putExtra("parent",replyList.get(realPosition).rpid);
                intent.putExtra("isDynamic", isDynamic);
                if(root!=0) intent.putExtra("parentSender",replyList.get(realPosition).sender.name);
                else intent.putExtra("parentSender","");
                context.startActivity(intent);
            });
        }
    }

    public void startReplyInfoActivity(long rpid, long oid, int type) {
        Intent intent = new Intent();
        intent.setClass(context, ReplyInfoActivity.class);
        intent.putExtra("rpid", rpid);
        intent.putExtra("oid", oid);
        intent.putExtra("type",type);
        context.startActivity(intent);
    }
    @Override
    public int getItemCount() {
        return replyList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }

    public static class ReplyHolder extends RecyclerView.ViewHolder{
        ImageView replyAvatar,dislikeBtn;
        CustomListView childReplies;
        TextView message,userName,pubDate,childCount,likeCount,replyBtn,upLiked,imageCount;
        LinearLayout childReplyCard;
        ImageView imageCard;


        public ReplyHolder(@NonNull View itemView) {
            super(itemView);

            replyAvatar = itemView.findViewById(R.id.replyAvatar);
            dislikeBtn = itemView.findViewById(R.id.dislikeBtn);
            childReplies = itemView.findViewById(R.id.repliesList);
            message = itemView.findViewById(R.id.replyText);
            userName = itemView.findViewById(R.id.replyUsername);
            pubDate = itemView.findViewById(R.id.replyPubDate);
            childCount = itemView.findViewById(R.id.repliesControl);
            likeCount = itemView.findViewById(R.id.likes);
            replyBtn = itemView.findViewById(R.id.replyBtn);
            upLiked = itemView.findViewById(R.id.upLiked);
            childReplyCard = itemView.findViewById(R.id.repliesCard);
            imageCount = itemView.findViewById(R.id.imageCount);
            imageCard = itemView.findViewById(R.id.imageCard);
        }
    }

    public static class WriteReply extends RecyclerView.ViewHolder{
        MaterialButton write_reply, sort;

        public WriteReply(@NonNull View itemView) {
            super(itemView);

            write_reply = itemView.findViewById(R.id.write_reply);
            sort = itemView.findViewById(R.id.sort);
        }
    }
}

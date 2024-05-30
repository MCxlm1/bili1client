package com.RobinNotBad.BiliClient.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.activity.CopyTextActivity;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.At;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;

//2023-07-25

@SuppressLint("ClickableViewAccessibility")
public class ToolsUtil {
    public static String toWan(long num){
        if(num>=10000){
            return String.format(Locale.CHINA, "%.1f", (float)num/10000) + "万";
        }
        else return String.valueOf(num);
    }

    public static String htmlToString(String html){
        return html.replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&quot;","\"")
                .replace("&amp;","&")
                .replace("&#39;", "'")
                .replace("&#34;", "\"")
                .replace("&#38;", "&")
                .replace("&#60;", "<")
                .replace("&#62;", ">");
    }

    public static String htmlReString(String html){
        return html.replace("<p>","")
                .replace("</p>","\n")
                .replace("<br>","\n")
                .replace("<em class=\"keyword\">","")
                .replace("</em>","");
    }

    public static String stringToFile(String str){
        return str.replace("|", "｜")
                .replace(":", "：")
                .replace("*", "﹡")
                .replace("?", "？")
                .replace("\"", "”")
                .replace("<", "＜")
                .replace(">", "＞")
                .replace("/", "／")
                .replace("\\", "＼");    //文件名里不能包含非法字符
    }

    public static String unEscape(String str){
        return str.replaceAll("\\\\(.)","$1");
    }

    public static int dp2px(float dpValue, Context context)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(float spValue,Context context)
    {
        final float fontScale = context.getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String getFileNameFromLink(String link){
        int length = link.length();
        for (int i = length - 1; i > 0; i--) {
            if(link.charAt(i)=='/'){
                return link.substring(i+1);
            }
        }
        return "fail";
    }

    public static String getFileFirstName(String file){
        for (int i = 0; i < file.length(); i++) {
            if(file.charAt(i)=='.'){
                return file.substring(0,i);
            }
        }
        return "fail";
    }

    public static void setCopy(TextView textView, Context context, String customText){
        if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener(view1 -> {
                Intent intent = new Intent(context, CopyTextActivity.class);
                intent.putExtra("content", customText == null ? textView.getText().toString() : customText);
                context.startActivity(intent);
                return true;
            });
        }
    }

    public static void setCopy(TextView textView, Context context){
        setCopy(textView, context, null);
    }

    public static void setCopy(Context context, TextView... textViews){
        for (TextView textView : textViews) {
            setCopy(textView, context, null);
        }
    }

    private static final Pattern bvPattern = Pattern.compile("BV[A-Za-z0-9]{10}");
    private static final Pattern avPattern = Pattern.compile("av\\d{1,10}");
    private static final Pattern cvPattern = Pattern.compile("cv\\d{1,10}");
    public static void setLink(TextView... textViews) {
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LINK_ENABLE, true)) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            Pattern urlPattern = Patterns.WEB_URL;
            Matcher urlMatcher = urlPattern.matcher(text);
            while (urlMatcher.find()) {
                int start = urlMatcher.start();
                int end = urlMatcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), LinkClickableSpan.TYPE_WEB_URL),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            Matcher matcher;

            matcher = bvPattern.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), LinkClickableSpan.TYPE_BVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            matcher = avPattern.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), LinkClickableSpan.TYPE_AVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            matcher = cvPattern.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), LinkClickableSpan.TYPE_CVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }

    public static void setAtLink(Map<String, Long> atUserUids, TextView... textViews) {
        if (atUserUids == null || atUserUids.isEmpty()) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            for (Map.Entry<String, Long> entry : atUserUids.entrySet()) {
                String key = entry.getKey();
                long val = entry.getValue();

                Pattern pattern = Pattern.compile("@" + key);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), LinkClickableSpan.TYPE_USER, String.valueOf(val)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }

    public static void setAtLink(List<At> ats, TextView... textViews) {
        if (ats == null || ats.isEmpty()) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            for (At at : ats) {
                spannableString.setSpan(new LinkClickableSpan(text.substring(at.textStartIndex, at.textEndIndex), LinkClickableSpan.TYPE_USER, String.valueOf(at.rid)),
                        at.textStartIndex, at.textEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }


    private static class LinkClickableSpan extends ClickableSpan {
        public static final int TYPE_USER = -1;
        public static final int TYPE_WEB_URL = 0;
        public static final int TYPE_BVID = 1;
        public static final int TYPE_AVID = 2;
        public static final int TYPE_CVID = 3;
        private final String text;
        /**
         * 真实值
         */
        private final String val;
        private final int type;

        public LinkClickableSpan(String text, int type, String val) {
            this.text = text;
            this.type = type;
            this.val = val;
        }

        public LinkClickableSpan(String text, int type) {
            this(text, type, null);
        }

        @Override
        public void onClick(View widget) {
            switch (type) {
                case TYPE_USER:
                    widget.getContext().startActivity(new Intent(widget.getContext(), UserInfoActivity.class).putExtra("mid", Long.parseLong(val)));
                    break;
                case TYPE_WEB_URL:
                    handleWebURL(widget.getContext(), text);
                    break;
                case TYPE_BVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), VideoInfoActivity.class).putExtra("bvid", text));
                    break;
                case TYPE_AVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), VideoInfoActivity.class).putExtra("aid", Long.parseLong(text.replace("av", ""))));
                    break;
                case TYPE_CVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), ArticleInfoActivity.class).putExtra("cvid", Long.parseLong(text.replace("cv", ""))));
                    break;
            }
        }

        private void handleWebURL(Context context, String text) {
            try {
                text = (text.startsWith("http://") || text.startsWith("https://") ? text : "http://" + text);
                // 很傻逼的一系列解析
                URL url = new URL(text);
                String path = url.getPath();
                int index = path.indexOf('?');
                if (index != -1) {
                    path = path.substring(0, index);
                }
                String domain = url.getHost();
                if (domain.equals("b23.tv")) {
                    handleShortUrl(context, text);
                    return;
                }
                if (domain.matches(".*\\.bilibili\\.com$")) {
                    if (!path.isEmpty()) {
                        String lastPathItem = path.replaceAll(".*/([^/]+)/?$", "$1");
                        Pair<String, Integer> parse = parseId(lastPathItem);
                        if (parse != null) {
                            String val = parse.first;
                            int type = parse.second;
                            switch (type) {
                                case TYPE_BVID:
                                    context.startActivity(new Intent(context, VideoInfoActivity.class).putExtra("bvid", val));
                                    return;
                                case TYPE_AVID:
                                    context.startActivity(new Intent(context, VideoInfoActivity.class).putExtra("aid", Long.parseLong(val.replace("av", ""))));
                                    return;
                                case TYPE_CVID:
                                    context.startActivity(new Intent(context, ArticleInfoActivity.class).putExtra("cvid", Long.parseLong(val.replace("cv", ""))));
                                    return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(text)));
            } catch (ActivityNotFoundException e) {
                MsgUtil.toast("没有可处理此链接的应用！", context);
            } catch (Throwable th) {
                MsgUtil.err(th, context);
            }
        }

        private void handleShortUrl(Context context, String url) {
            CenterThreadPool.run(() -> {
                try {
                    Response response = NetWorkUtil.get(url, NetWorkUtil.webHeaders, location -> handleWebURL(context, location));
                    ResponseBody body;
                    if (response.code() == 200 && (body = response.body()) != null) {
                        JSONObject json = new JSONObject(body.string());
                        if (json.has("code") && json.getInt("code") == -404) {
                            CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("啥都木有~", context));
                        }
                    }
                } catch (IOException | JSONException e) {
                    CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("解析失败！", context));
                }
            });
        }

        private Pair<String, Integer> parseId(String lastPathItem) {
            Matcher matcher;

            matcher = bvPattern.matcher(lastPathItem);
            if (matcher.find()) {
                return new Pair<>(matcher.group(), TYPE_BVID);
            }

            matcher = avPattern.matcher(lastPathItem);
            if (matcher.find()) {
                return new Pair<>(matcher.group(), TYPE_AVID);
            }

            matcher = cvPattern.matcher(lastPathItem);
            if (matcher.find()) {
                return new Pair<>(matcher.group(), TYPE_CVID);
            }

            return null;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setColor(Color.parseColor("#03a9f4"));
        }
    }

    // 查到的一种LinkMovementMethod问题的解决方法
    public static class ClickableSpanTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!(v instanceof TextView)) {
                return false;
            }
            TextView widget = (TextView) v;
            CharSequence text = widget.getText();
            if (!(text instanceof Spanned)) {
                return false;
            }
            Spanned buffer = (Spanned) text;
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

                if (links.length != 0) {
                    ClickableSpan link = links[0];
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(widget);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}

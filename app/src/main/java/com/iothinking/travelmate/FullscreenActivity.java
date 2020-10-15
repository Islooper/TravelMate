package com.iothinking.travelmate;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.iothinking.travelmate.utils.HttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    AutoScrollTextView autoScrollTextView;

    TextView totaled , totaling , parkTv , temTv , fvTv , noiseTv;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> mPermissionList = new ArrayList<>();

    private static final int PERMISSION_REQUEST = 1;
// 检查权限

    private void checkPermission() {
        mPermissionList.clear();
        //判断哪些权限未授予
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(FullscreenActivity.this, permissions, PERMISSION_REQUEST);
        }
    }
    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    ImageView img;
    RecyclerView recyclerview;

    private Integer[] mImgIds = {R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4, R.drawable.p5, R.drawable.p6};
    private List<Integer> datas;
    private RecyAdapter recyAdapter;
    private Handler mHandler=new Handler();
    private LinearLayoutManager layoutManager;

    private int oldItem=0;

    private void initRecyleView() {
        img = findViewById(R.id.img);
        recyclerview = findViewById(R.id.recyclerview);
        initData();
        initRecy();
        img.setImageResource(datas.get(0));
        recyAdapter.setOnItemClickListener(new RecyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int tag) {

            }
        });
    }

    Runnable scrollRunnable=new Runnable() {
        @Override
        public void run() {
            recyclerview.scrollBy(3,0);

            int firstItem=layoutManager.findFirstVisibleItemPosition();
            if(firstItem!=oldItem&&firstItem>0){
                oldItem=firstItem;
                img.setImageResource(datas.get(oldItem%datas.size()));
            }
            mHandler.postDelayed(scrollRunnable,100);
        }
    };

    private void initRecy() {
        recyAdapter=new RecyAdapter(this,datas);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setAdapter(recyAdapter);
    }

    private void initData() {
        datas=new ArrayList<>();
        for (int i = 0; i <mImgIds.length ; i++) {
            datas.add(mImgIds[i]);
        }
    }

    private MediaPlayer mediaPlayer;
    private void initMedia() {
        final VideoView videoView = (VideoView)findViewById(R.id.videoView);
        //加载指定的视频文件
        String path = "/storage/emulated/0/Movies/0708.mp4";
        videoView.setVideoPath(path);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.setVideoPath("/storage/emulated/0/Movies/0708.mp4");
                videoView.start();
            }
        });
        mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource("/storage/emulated/0/Movies/colorsofthewind.mp3");
//            mediaPlayer.setLooping(true);//设置为循环播放
//            mediaPlayer.prepare();//初始化播放器MediaPl
//            mediaPlayer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(scrollRunnable,10);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(scrollRunnable);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        initRecyleView();

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        Log.e("DISPLAY", "Width:"+point.x+",height:"+point.y);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        totaled = findViewById(R.id.tv_totaled);
        totaling = findViewById(R.id.tv_totaling);
        parkTv = findViewById(R.id.tv_park);
        temTv = findViewById(R.id.tv_tem);
        fvTv = findViewById(R.id.tv_fv);
        noiseTv = findViewById(R.id.tv_noise);
        //启动公告滚动条

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        initMedia();


        // 广播接受
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);


        // 获取入园情况
        getTheEnterPark();


    }

    private void getTheEnterPark() {
        // 获取当天的时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        String startTime = df.format(new Date());
        startTime = startTime +" 00:00:00";

        // 获取当前时间
        SimpleDateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String endTime = dff.format(new Date());

        // 获取当前游客
        HttpUtils.getStatusPark("1" , startTime , endTime);

        // 获取累计游客
        HttpUtils.getStatusPark("2" , startTime , endTime);

        // 获取剩余停车位
        HttpUtils.readParkSpace();

        // 获取温度
        HttpUtils.getSensorData("001" , "8");

        // 获取风速
        HttpUtils.getSensorData("001" , "52");

        // 获取噪声
        HttpUtils.getSensorData("001" , "17");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * 广播
     */
    public static final String BROADCAST_ACTION = "com.example.corn";
    private BroadcastReceiver mBroadcastReceiver;

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }



    /**
     * 广播接受类
     */

    class MyBroadcastReceiver extends BroadcastReceiver {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("countsing");

            if (value != null && !value.equals("")){
                totaling.setText(value+"人次");
            }

            String valued = intent.getStringExtra("countsed");
            if (valued != null && !valued.equals("")){
                totaled.setText(valued+"人次");
            }

            String park = intent.getStringExtra("park");
            if (park != null && !park.equals("")){
                parkTv.setText(park+"个");
            }

            // 温度
            String tem = intent.getStringExtra("tem");
            if (tem != null && !tem.equals("")){
                String[] temArr = tem.split(",");
                temTv.setText(temArr[0]+"摄氏度");
            }

            // 风速
            String fv = intent.getStringExtra("fv");
            if (fv != null && !fv.equals("")){
                fvTv.setText(fv);
            }

            // 噪音
            String noise = intent.getStringExtra("noise");
            if (noise != null && !noise.equals("")){
                if (noise.equals("1")){
                    noiseTv.setText("噪音一般");
                }else {
                    noiseTv.setText("无噪音");
                }
            }
        }

    }

}

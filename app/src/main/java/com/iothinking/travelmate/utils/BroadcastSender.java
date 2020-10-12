package com.iothinking.travelmate.utils;

import android.content.Intent;

import com.iothinking.travelmate.MyApplication;
import static com.iothinking.travelmate.FullscreenActivity.BROADCAST_ACTION;

/**
 * Created by looper on 2020/9/14.
 */
public class BroadcastSender {
    Intent intent = new Intent();
    public void send(String name ,String value)
    {
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(name, value);
        MyApplication.getmContext().sendBroadcast(intent);
    }
}

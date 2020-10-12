package com.iothinking.travelmate.utils;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

/**
 * Created by looper on 2020/10/12.
 */
public class HttpUtils {


    /**
     * 获取入园状态信息
     * @param status：0-未使用，1-使用中，2-已使用
     */
    public static void getStatusPark(String status , String startTime , String endTime){
        OkHttpUtils.get().url(Url.URL_DEV + "ticket/timeGetTicketOrder.do")
                .addParams("startTime", startTime)
                .addParams("endTime", endTime)
                .addParams("usedStatus", status)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response)
                    {
                        Log.e("back data : ", response);
                        JSONObject data = JSONObject.parseObject(response);
                        int code = data.getInteger("resultCode");
                        if (code == 0) {
                            // TODO 计算人数 包含正在游玩人数 和 当天累计游玩人数

                            // 根据用途不同发送不同的广播
                            // 发送数据成功广播
                            BroadcastSender sender = new BroadcastSender();
                            // TODO 发送广播通知界面

//                            switch (use){
//                                case 1:
//                                    sender.send("sensors","ok");
//                                    break;
//                                case 2:
//                                    sender.send("sensors","refresh");
//                                    break;
//                            }


                        }

                    }
                });

    }
}

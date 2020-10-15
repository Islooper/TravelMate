package com.iothinking.travelmate.utils;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
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
    public static void getStatusPark(final String status , final String startTime , String endTime){
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
                        JSONObject  data = JSONObject.parseObject(response);
                        int code = data.getInteger("resultCode");
                        if (code == 0) {
                            String result  = data.getString("result");
                           // JSONObject results = JSONObject.parse(result);
                            // 根据用途不同发送不同的广播
                            // 发送数据成功广播
                            BroadcastSender sender = new BroadcastSender();
                            JSONArray jsonArray = JSONArray.parseArray(result);
                            int counts = 0;
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject= jsonArray.getJSONObject(i);
                                Integer count = jsonObject.getInteger("count");
                                counts = counts + count;
                            }
                            if (status.equals("1")){
                                sender.send("countsing",String.valueOf(counts));
                            } else if (status.equals("2")) {
                                // 累计游玩人数
                                sender.send("countsed",String.valueOf(counts));
                            }
                        }

                    }
                });

    }


    public static void readParkSpace(){
        OkHttpUtils.get()
                .url(Url.URL_DEV + "park/parkIdReadTruckSpace")
                .addParams("parkId" , "1")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Log.e("readParkSpace","请求错误");
                    }

                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        Integer resultCode = jsonObject.getInteger("resultCode");

                        if (resultCode == 0){
                            String result = jsonObject.getString("result");
                            JSONArray jsonArray = JSONArray.parseArray(result);
                            BroadcastSender sender = new BroadcastSender();
                            sender.send("park" , String.valueOf(jsonArray.size()));

                        }
                    }
                });
    }

    /**
     * 获取一些传感器数据
     */

    public static void getSensorData(String place , final String types){
        OkHttpUtils.get()
                .url(Url.URL_DEV + "placeAndTypeSelectSensorData.do")
                .addParams("place" , place)
                .addParams("types" , types)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        Log.e("res" , response);
                        JSONObject jsonObject = JSONObject.parseObject(response);
                        Integer resultCode = jsonObject.getInteger("resultCode");

                        String value = "";
                        if (resultCode == 0){
                            String result = jsonObject.getString("result");
                            JSONArray jsonArray = JSONArray.parseArray(result);

                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject j= jsonArray.getJSONObject(i);
                                value = j.getString("value");
                            }
                        }

                        BroadcastSender sender = new BroadcastSender();
                        switch (types){
                            case "8":
                                sender.send("tem" , value);
                                break;
                            case "52":
                                sender.send("fv" , value);
                                break;
                            case "17":
                                sender.send("noise" , value);
                                break;
                        }

                    }
                });
    }

}

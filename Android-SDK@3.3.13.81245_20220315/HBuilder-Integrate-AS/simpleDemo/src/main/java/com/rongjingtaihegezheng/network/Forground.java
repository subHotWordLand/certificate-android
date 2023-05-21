package com.rongjingtaihegezheng.network;

import org.json.JSONObject;

import java.util.HashMap;

public class Forground extends BaseNetApi{
    private String longlatUrl = BaseNetApiConfig.getInstance().getBaseDomain() + "/api/yiqing/latitudeAndLongitude";
    /**
     * 记录接口
     * @param listener callback
     */
    public void record(final HashMap<String, Object> params , final OnResultListener listener) {
        new Thread() {
            public void run() {
                try {
                    String result = doPostJson(longlatUrl, params);
                    JSONObject resultJsonObject = new JSONObject(result);
//                    if (!checkResponseSuccess(result)){
//                        Exception e = new Exception(getResponseMessage(result));
//                        listener.fail(e);
//                        return;
//                    }
                    listener.success(null);
                } catch (Exception e) {
                    listener.fail(e);
                }
            }
        }.start();
    }
}

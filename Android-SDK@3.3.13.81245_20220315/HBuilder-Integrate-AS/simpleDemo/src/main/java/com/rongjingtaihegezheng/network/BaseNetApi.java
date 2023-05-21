package com.rongjingtaihegezheng.network;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseNetApi {
    public interface OnResultListener {
        void success(Object result);
        void fail(Exception e);
    }

    /**
     * 优雅的将Object转换成List
     * @param obj object
     * @param clazz converted class name
     * @param <T> type
     * @return result
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
    public Boolean checkResponseSuccess(String responseString){
        try {
            JSONObject object = new JSONObject(responseString);
            return object.getBoolean("success");
        }catch (Exception e){
            return false;
        }
    }
    public String getResponseMessage(String responseString){
        try {
            JSONObject object = new JSONObject(responseString);
            return object.getString("message");
        }catch (Exception e){
            return "未知错误";
        }
    }
    /**
     * post 请求
     * @param url
     * @param parameter 参数
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, Object> parameter) throws Exception {
//        User user = User.getInstance();
//        String token = user.getToken();
        HashMap<String,Object> header = new HashMap<>();
//        if (token != null){
//            header.put("Authorization",token);
//            header.put("token",token);
//        }
        try{
            String result = BaseNetwork.doPost(url,header,parameter);
            JSONObject resultJsonObject = new JSONObject(result);
            int code = -1;
            try{
                code = resultJsonObject.getInt("code");
            }catch (Exception e){
            }
            if (code == 401){
                //登录过期 需要重新登录
                /**
                 * 将界面跳转到登录界面，并将栈中所有Activity清空
                 * startActivity的时候传递FLAG_ACTIVITY_CLEAR_TASK这个标志,
                 * 那么这个标志将会清除之前所有已经打开的activity.然后将会变成另外一个空栈的root,
                 * 然后其他的Activitys就都被关闭了.这个方法必须跟着{@link #FLAG_ACTIVITY_NEW_TASK}一起使用.
                 * 一定要和FLAG_ACTIVITY_NEW_TASK一起使用
                 */
//                Intent intent = new Intent(MyActivityManager.getInstance().getCurrentActivity(), LoginActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                MyActivityManager.getInstance().getCurrentActivity().startActivity(intent);
//                String message = resultJsonObject.getString("message");
//                Handler mainHandler = new Handler(Looper.getMainLooper());
//                mainHandler.post(() -> {
//                    ToastUtil.getInstance().showToast(message);
//                });
            }
            return result;
        }catch (Exception e){
            Log.e("BaseNetApi",e.toString());
            throw e;
        }
    }
    /**
     * post 请求
     * @param url
     * @param parameter 参数
     * @return
     * @throws Exception
     */
    public static String doPostJson(String url, HashMap<String, Object> parameter ) throws Exception {
        // Iterating entries using a For Each loop
        JSONObject jsonParameter = new JSONObject();
        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            jsonParameter.put(key,value);
        }
//        User user = User.getInstance();
        String token = BaseNetApiConfig.getInstance().getToken();
        HashMap<String,Object> header = new HashMap<>();
        header.put("Authorization",token);
        try{
            String result = BaseNetwork.doPost(url,header,String.valueOf(jsonParameter));
            JSONObject resultJsonObject = new JSONObject(result);
            return result;
        }catch (Exception e){
            Log.e("BaseNetApi",e.toString());
            throw e;
        }
    }
}

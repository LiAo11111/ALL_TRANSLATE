package com.example.simpleocr;

// HTTP 请求工具类

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final String BASE_URL = "暂时隐藏，不可查看";
    private static final OkHttpClient client = new OkHttpClient().newBuilder().build();
    public static Map post(String route, String json) {
       try
       {
           RequestBody body = RequestBody.create(json, JSON);
           Request request = new Request.Builder()
                   .url(BASE_URL + route)
                   .method("POST", body)
                   .addHeader("Content -Type", "application/json")
                   .build();
           Response response = client.newCall(request).execute();
           Gson gson = new Gson();
           Map ret = new HashMap<>();
           ret = gson.fromJson(response.body().string(), ret.getClass());
           ret.put("code",response.code());
           return ret;
       }
       catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }
}

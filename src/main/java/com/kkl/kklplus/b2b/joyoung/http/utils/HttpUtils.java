package com.kkl.kklplus.b2b.joyoung.http.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.joyoung.http.response.ProductData;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.utils.SpringContextHolder;
<<<<<<< .mine
||||||| .r11663
import okhttp3.*;
=======
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
>>>>>>> .r11765

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

/**
 * @Auther wj
 * @Date 2020/11/17 10:51
 */
public class HttpUtils {



    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

<<<<<<< .mine
    public static <T> ResponseBody<T> getRequest(String url, Map<String, String> params, Class<T> dataClass) {
        ResponseBody<T> responseBody = null;
||||||| .r11663
    public static String getRequest( String url, Map<String, String> params) {

=======
    public static ResponseBody<ProductData> getRequest(String url, Map<String, String> params) {
>>>>>>> .r11765
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        ResponseBody<ProductData> responseBody = new ResponseBody<>(ResponseBody.ErrorCode.SUCCESS);
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
<<<<<<< .mine
                if (response.body() != null) {
                    String responseBodyJson = response.body().string();
                    try {
                        responseBody = gson.fromJson(responseBodyJson, new TypeToken<ResponseBody>() {
                        }.getType());
                        responseBody.setOriginalJson(responseBodyJson);
                        try {
                            T data = gson.fromJson(responseBodyJson, dataClass);
                            responseBody.setData(data);
                        } catch (Exception e) {
                            return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
                        }
                    } catch (Exception e) {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                        responseBody.setOriginalJson(responseBodyJson);
                        return responseBody;
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
||||||| .r11663
                String toJson = response.body().string();
                ProductData productData= gson.fromJson(toJson,ProductData.class);
                if (productData.getProduct()!=null){
                    return toJson;
=======
                if (response.body() != null) {
                    String responseBodyJson = response.body().string();
                    try {
                        responseBody.setOriginalJson(responseBodyJson);
                        try {
                            ProductData data = gson.fromJson(responseBodyJson, ProductData.class);
                            responseBody.setData(data);
                        } catch (Exception e) {
                            return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
                        }
                    } catch (Exception e) {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                        responseBody.setOriginalJson(responseBodyJson);
                        return responseBody;
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
>>>>>>> .r11765
                }
<<<<<<< .mine

||||||| .r11663
=======
            } else {
                responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
>>>>>>> .r11765
            }
<<<<<<< .mine
            return responseBody;
        } catch (IOException e) {
            return responseBody;
||||||| .r11663
            return null;
        } catch (IOException e) {
            return null;
=======
        } catch (Exception e) {
            return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
>>>>>>> .r11765
        }
        return responseBody;
    }
}
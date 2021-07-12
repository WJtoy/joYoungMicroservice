package com.kkl.kklplus.b2b.joyoung.http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.config.B2BTooneProperties;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.utils.DateUtils;
import com.kkl.kklplus.b2b.joyoung.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class OkHttpUtils {

    private static final MediaType CONTENT_TYPE = MediaType.parse("text/xml;charset=UTF-8");

    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static B2BTooneProperties tooneProperties = SpringContextHolder.getBean(B2BTooneProperties.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static <T> ResponseBody<T> postSyncGenericNew(OperationCommand command, Class<T> dataClass) {
        ResponseBody<T> responseBody = new ResponseBody<>();
        B2BTooneProperties.DataSourceConfig dataSourceConfig = tooneProperties.getDataSourceConfig();
        if (dataSourceConfig != null && command != null && command.getOpCode() != null &&
                command.getReqBody() != null && command.getReqBody().getClass().getName().equals(command.getOpCode().reqBodyClass.getName())) {
            command.getReqBody().setAppKey(dataSourceConfig.getAppKey());
            command.getReqBody().setAppSecret(dataSourceConfig.getAppSecret());
            PostObject post = new PostObject();
            post.setOperation(dataSourceConfig.getOperation());
            post.setSoapAction(dataSourceConfig.getSoapAction());
            post.setTargetNamespace(dataSourceConfig.getTargetNamespace());
            post.setSoapVersion(PostObject.SOAP1_1);
            //设置调用方法参数，顺序需和方法定义一致
            Map<String, String> params =  post.getParameter();
            BASE64Encoder encode64 = new BASE64Encoder();
            String userPwd = null;
            try {
                userPwd = encode64.encode(dataSourceConfig.getUserPwd().getBytes("UTF-8"));
            }catch (Exception e){
                e.printStackTrace();
            }
            String serviceCode = command.getOpCode().serviceCode;
            String serviceVersion = dataSourceConfig.getServiceVersion();
            String userName = dataSourceConfig.getUserName();
            String parm = gson.toJson(command.getReqBody());;
            String sign = getMD5Str(serviceCode+serviceVersion+userName+parm+
                    DateUtils.formatDate(
                        DateUtils.timeStampToDate(System.currentTimeMillis()
                    ),"yyyyMMdd"));
            params.put("serviceCode", serviceCode);
            params.put("serviceVersion", serviceVersion);
            params.put("userName", userName);
            params.put("userPwd", userPwd);
            params.put("sign", sign);
            params.put("parm", parm);
            params.put("page", "{}");
            post.buildContent();
            List<String> result = new ArrayList<String>();
            RequestBody requestBody = RequestBody.create(CONTENT_TYPE, post.getContent());
            Request.Builder requestBuilder = new Request.Builder()
                    .url(dataSourceConfig.getRequestMainUrl())
                    .post(requestBody);
            if(post.getSoapAction().equals(PostObject.SOAP1_1)){
                requestBuilder.addHeader("SOAPAction", post.getSoapAction());
            }else if(post.getSoapAction().equals(PostObject.SOAP1_2)){

            }else{
                requestBuilder.addHeader("SOAPAction", post.getSoapAction());
            }
            Call call = okHttpClient.newCall(requestBuilder.build());
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBodyJson = response.body().string();
                        try {
                            Document doc = DocumentHelper.parseText(responseBodyJson);
                            Element root = doc.getRootElement();
                            Element select = (Element)root.clone();
                            int x_path = 2;
                            if(root.hasContent()){
                                for(int i=0; i< x_path; i++){
                                    Element child = (Element)select.elements().get(0);
                                    select = child;
                                }
                                List<String> list = post.getElementList(select);
                                result.addAll(list);
                            }
                            responseBody.setOriginalJson(gson.toJson(result));
                            responseBody.setErrorCode(Integer.valueOf(result.get(0)));
                            if (result.get(0).equals(ResponseBody.ErrorCode.SUCCESS.code+"")) {
                                try {
                                    T data = gson.fromJson(result.get(1), dataClass);
                                    responseBody.setOriginalJson(result.get(1));
                                    responseBody.setData(data);
                                } catch (Exception e) {
                                    return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
                                }
                            }else{
                                responseBody.setErrorMsg(result.get(1));
                            }
                        } catch (Exception e) {
                            if(result.size() <= 0) {
                                log.error("数据处理报错原因是：" + e.getMessage() + "\n原始数据：" + responseBodyJson);
                            }
                            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                            return responseBody;
                        }
                    } else {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
                }
            } catch (Exception e) {
                return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
            }
        } else {
            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_PARAMETER_FORMAT_ERROR);
        }

        return responseBody;
    }
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {

        } catch (UnsupportedEncodingException e) {

        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            }else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString();
    }
}

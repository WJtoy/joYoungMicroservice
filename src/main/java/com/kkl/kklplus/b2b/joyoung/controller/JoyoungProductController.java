package com.kkl.kklplus.b2b.joyoung.controller;

<<<<<<< .mine
import com.kkl.kklplus.b2b.joyoung.http.response.ProductData;
||||||| .r11663
=======
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.response.ProductData;
>>>>>>> .r11765
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.utils.HttpUtils;
<<<<<<< .mine
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
||||||| .r11663
=======
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
>>>>>>> .r11765
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
<<<<<<< .mine
import com.kkl.kklplus.entity.b2b.rpt.B2BProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.utils.QuarterUtils;
||||||| .r11663
=======
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.viomi.sd.ProductParts;
import com.kkl.kklplus.utils.QuarterUtils;
>>>>>>> .r11765
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther wj
 * @Date 2020/11/17 11:01
 */


@Slf4j
@RestController
@RequestMapping("/product")
public class JoyoungProductController {

<<<<<<< .mine
    @Autowired
    private B2BProcesslogService b2BProcesslogService;


    @GetMapping("/barCodeVerify/{productCode}")
    public MSResponse<ResponseBody> getProductData(@PathVariable String productCode){
        try {
            B2BOrderProcesslog b2BProcesslog= new B2BOrderProcesslog();
            b2BProcesslog.setUpdateById(1L);
            b2BProcesslog.setProcessTime(1);
            b2BProcesslog.setProcessFlag(1);
            b2BProcesslog.setInterfaceName("barCodeVerify");
            b2BProcesslog.setCreateById(1L);
            b2BProcesslog.setInfoJson(productCode);
            b2BProcesslog.setCreateDt(System.currentTimeMillis());
            b2BProcesslog.setQuarter(QuarterUtils.getQuarter(new Date()));
            b2BProcesslogService.insert(b2BProcesslog);
            Map<String,String> map = new HashMap<String,String>();
            map.put("method", "getProductInfoByCode");
            map.put("product_code", productCode);
            ResponseBody<ProductData> dataResponseBody =  HttpUtils.getRequest("http://ccplan.joyoung.com/jyccplan/api/customer",map,ProductData.class);
            if (dataResponseBody !=null && dataResponseBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code &&  dataResponseBody.getData()!= null) {
                b2BProcesslog.setProcessComment(dataResponseBody.getErrorMsg());
                b2BProcesslog.setUpdateDt(System.currentTimeMillis());
                b2BProcesslog.setResultJson(dataResponseBody.getOriginalJson());
                b2BProcesslog.setProcessFlag(4);

                b2BProcesslogService.updateProcessFlag(b2BProcesslog);

            }else {
                b2BProcesslog.setProcessComment(dataResponseBody.getErrorMsg());
                b2BProcesslog.setUpdateDt(System.currentTimeMillis());
                b2BProcesslog.setResultJson(dataResponseBody.getOriginalJson());
                b2BProcesslog.setProcessFlag(3);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);

            }

        }catch (Exception e){

||||||| .r11663
    @GetMapping("/barCodeVerify/{productCode}")
    public MSResponse<ResponseBody> getProductData(@PathVariable String productCode){
        Map<String,String> map = new HashMap<String,String>();
        map.put("method", "getProductInfoByCode");
        map.put("product_code", productCode);
        String result =  HttpUtils.getRequest("http://ccplan.joyoung.com/jyccplan/api/customer",map);
        if (result!=null ) {
            return new MSResponse(MSErrorCode.SUCCESS);
=======
    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @GetMapping("/barCodeVerify/{productBarCode}")
    public MSResponse getProductData(@PathVariable String productBarCode){
        if("TYM00301891".equals(productBarCode)){
            return new MSResponse(MSErrorCode.SUCCESS);
>>>>>>> .r11765
        }
<<<<<<< .mine

        return new MSResponse(MSErrorCode.FAILURE,"查无此产品数据");
||||||| .r11663
        return new MSResponse(MSErrorCode.FAILURE,"查无此产品数据");
=======
        Map<String,String> map = new HashMap<>();
        map.put("method", "getProductInfoByCode");
        map.put("product_code", productBarCode);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInfoJson(new Gson().toJson(map));
        b2BProcesslog.setInterfaceName("getProductInfoByCode");
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.setCreateDt(System.currentTimeMillis());
        b2BProcesslog.setUpdateDt(System.currentTimeMillis());
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(new Date()));
        b2BProcesslog.setProcessFlag(0);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslogService.insert(b2BProcesslog);
        ResponseBody<ProductData> resBody =
                HttpUtils.getRequest("http://ccplan.joyoung.com/jyccplan/api/customer",map);
        if ( resBody != null && resBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code) {
            b2BProcesslog.setProcessComment(resBody.getErrorMsg());
            b2BProcesslog.setUpdateDt(System.currentTimeMillis());
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            b2BProcesslog.setProcessFlag(4);
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            ProductData data = resBody.getData();
            if(data != null && data.getProduct() != null) {
                return new MSResponse(MSErrorCode.SUCCESS);
            }else{
                return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),"查无此产品数据"));
            }
        }else {
            b2BProcesslog.setProcessComment(resBody.getErrorMsg());
            b2BProcesslog.setUpdateDt(System.currentTimeMillis());
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            b2BProcesslog.setProcessFlag(3);
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(resBody.getErrorMsg(),200)));
        }
>>>>>>> .r11765
    }





}

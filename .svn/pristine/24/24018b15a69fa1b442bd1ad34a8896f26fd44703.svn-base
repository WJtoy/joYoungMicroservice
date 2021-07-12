package com.kkl.kklplus.b2b.joyoung.controller;

import com.github.pagehelper.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderConfirmed;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderInfo;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.config.B2BTooneProperties;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderConfirmedRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.request.OrderListRequestParam;
import com.kkl.kklplus.b2b.joyoung.http.response.OrderListResponseData;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseBody;
import com.kkl.kklplus.b2b.joyoung.http.response.ResponseData;
import com.kkl.kklplus.b2b.joyoung.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BWorkcardQtyDailyMQSend;
import com.kkl.kklplus.b2b.joyoung.service.*;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.order.B2BWorkcardQtyDaily;
import com.kkl.kklplus.entity.b2b.pb.MQB2BWorkcardQtyDailyMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BShopEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/joyoungOrderInfo")
public class JoyoungOrderInfoController {

    @Autowired
    private B2BTooneProperties tooneProperties;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BWorkcardQtyDailyMQSend b2BWorkcardQtyDailyMQSend;

    @Autowired
    private JoyoungOrderConfirmedService joyoungOrderConfirmedService;

    @Autowired
    private JoyoungOrderInfoService joyoungOrderInfoService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void joyoungJob() {
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig();
        if (!dataSourceConfig.getScheduleEnabled()) {
            return;
        }
        this.publicOrderJob();
    }
    private void publicOrderJob() {
        OrderListRequestParam reqBody = new OrderListRequestParam();
        reqBody.setOrderType(1);
        reqBody.setMaxQty(50);
        reqBody.setDates(180);
        reqBody.setCompanyName(tooneProperties.getDataSourceConfig().getCompanyName());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.ORDERLIST, reqBody);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.preInsert();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.ORDERLIST.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        ResponseBody<OrderListResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, OrderListResponseData.class);
        try {
            log.info("抓取得数据:"+resBody.getOriginalJson());
            //记录原始数据
            b2BProcesslog.setInfoJson(new Gson().toJson(reqBody));
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if (resBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code) {
                OrderListResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getCode() == ResponseBody.ErrorCode.SUCCESS.code) {
                    List<OrderListResponseData.JoyoungOrder> joyoungOrders = resBody.getData().getData();
                    if (joyoungOrders != null && joyoungOrders.size() > 0) {
                        List<JoyoungOrderConfirmed> joyoungOrderConfirmeds = joyoungOrderInfoService.insertManyOrderInfo(joyoungOrders);
                        if (joyoungOrderConfirmeds != null && joyoungOrderConfirmeds.size() > 0) {
                            //记录失败的订单
                            StringBuffer failureCcomment = new StringBuffer();
                            for (JoyoungOrderConfirmed joyoungOrderConfirmed : joyoungOrderConfirmeds) {
                                failureCcomment.append(joyoungOrderConfirmed.getOrderNo() + " ");
                            }
                            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_REJECT.value);
                            b2BProcesslog.setProcessComment(StringUtils.left("数据不全→失败的订单有" + failureCcomment.toString(), 200));
                            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                        } else {
                            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                        }
                        //调用接单的接口
                        orderConfirm(joyoungOrderConfirmeds);
                    } else {
                        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                        b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    }
                }else{
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(StringUtils.left(responseData != null?"":responseData.getMsg(),200));
                    b2BProcesslog.setResultJson(resBody.getOriginalJson());
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
            }else{
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                if(!StringUtils.isBlank(resBody.getErrorDetailMsg())){
                    b2BProcesslog.setProcessComment(StringUtils.left(resBody.getErrorDetailMsg(),200));
                }else {
                    b2BProcesslog.setProcessComment(StringUtils.left(resBody.getErrorMsg(),200));
                }
                b2BProcesslog.setResultJson(resBody.getOriginalJson());
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            }
        }catch (Exception e) {
            String errStr = "抓取工单失败 "+e.getMessage();
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(errStr,200));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            log.error(e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(reqBody),
                    StringUtils.left(errStr,200),
                    "抓取工单失败",OperationCommand.OperationCode.ORDERLIST.serviceCode, "POST");
        }
    }


    @PostMapping("/confirm")
    public MSResponse orderConfirm(@RequestBody List<JoyoungOrderConfirmed> joyoungOrderConfirmeds) {
        MSResponse msResponse = new MSResponse();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        if(joyoungOrderConfirmeds == null || joyoungOrderConfirmeds.size() <= 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            return msResponse;
        }
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.CONFIRM.serviceCode);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        OrderConfirmedRequestParam reqBody = new OrderConfirmedRequestParam();
        reqBody.setResult_flag(0);
        List<OrderConfirmedRequestParam.OrderResult> orderResults = new ArrayList<>();
        for(JoyoungOrderConfirmed joyoungOrderConfirmed : joyoungOrderConfirmeds){
            OrderConfirmedRequestParam.OrderResult orderResult = new OrderConfirmedRequestParam.OrderResult();
            orderResult.setOrderNo(joyoungOrderConfirmed.getOrderNo());
            orderResult.setThirdSendMessage(joyoungOrderConfirmed.getThirdSendMessage());
            orderResult.setThirdSendFlag(joyoungOrderConfirmed.getThirdSendFlag());
            orderResult.setOrderId(joyoungOrderConfirmed.getOrderId());
            orderResults.add(orderResult);
        }
        reqBody.setData(orderResults);
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CONFIRM, reqBody);
        ResponseBody<ResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseData.class);
        String infoJson = new Gson().toJson(reqBody);
        try {
            b2BProcesslog.setInfoJson(infoJson);
            b2BProcesslogService.insert(b2BProcesslog);
            joyoungOrderConfirmedService.insert(joyoungOrderConfirmeds);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                String error = StringUtils.left(resBody.getErrorMsg(),200);
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),error));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),error));
                joyoungOrderConfirmedService.updateProcessFlag(joyoungOrderConfirmeds,
                        B2BProcessFlag.PROCESS_FLAG_FAILURE.value,error,System.currentTimeMillis());
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(error);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                ResponseData responseData = resBody.getData();
                if(responseData != null && responseData.getResultFlag() == ResponseBody.ErrorCode.SUCCESS.code) {
                    joyoungOrderConfirmedService.updateProcessFlag(joyoungOrderConfirmeds,
                            B2BProcessFlag.PROCESS_FLAG_SUCESS.value,"",System.currentTimeMillis());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }else{
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),StringUtils.left(responseData != null?"":responseData.getResultMsg(),200)));
                    joyoungOrderConfirmedService.updateProcessFlag(joyoungOrderConfirmeds,
                            B2BProcessFlag.PROCESS_FLAG_FAILURE.value,StringUtils.left(responseData != null?"":responseData.getResultMsg(),200),System.currentTimeMillis());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(StringUtils.left(responseData != null?"":responseData.getResultMsg(),200));
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
                return msResponse;

            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(StringUtils.left(e.getMessage(),200));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "工单接单失败 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(1L,infoJson,errorStr + e.getMessage(),
                    errorStr,OperationCommand.OperationCode.CONFIRM.serviceCode, "POST");
            return msResponse;
        }
    }

    /**
     * 获取九阳工单(分页)
     * @param workcardSearchModel
     * @return
     */
    @PostMapping("/getList")
    public MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel) {
        Gson gson = new Gson();
        try {
            Page<JoyoungOrderInfo> orderInfoPage = joyoungOrderInfoService.getList(workcardSearchModel);
            Page<B2BOrder> customerPoPage = new Page<>();
            for(JoyoungOrderInfo orderInfo:orderInfoPage){
                B2BOrder customerPo = new B2BOrder();
                customerPo.setId(orderInfo.getId());
                customerPo.setB2bOrderId(orderInfo.getId());
                //数据源
                customerPo.setDataSource(B2BDataSourceEnum.JOYOUNG.id);
                customerPo.setOrderNo(orderInfo.getOrderNo());
                customerPo.setParentBizOrderId(orderInfo.getOrderNo());
                //康宝店铺
                customerPo.setShopId(B2BShopEnum.JOYOUNG.id);
                customerPo.setUserName(orderInfo.getUserName());
                customerPo.setUserMobile(orderInfo.getUserMobile());
                customerPo.setUserPhone(orderInfo.getUserPhone());
                customerPo.setUserAddress(orderInfo.getUserAddress());
                customerPo.setBrand(orderInfo.getBrand());
                customerPo.setRemarks(orderInfo.getRemarks());
                customerPo.setReceiveDate(orderInfo.getReceiveDate());
                customerPo.setDescription(orderInfo.getRemarks());
                customerPo.setStatus(1);
                customerPo.setIssueBy(orderInfo.getIssueBy());
                customerPo.setProcessFlag(orderInfo.getProcessFlag());
                customerPo.setProcessTime(orderInfo.getProcessTime());
                customerPo.setProcessComment(orderInfo.getProcessComment());
                customerPo.setQuarter(orderInfo.getQuarter());
                List<OrderListResponseData.JoyoungOrder.Product> products =
                        gson.fromJson(orderInfo.getItemsJson(),new TypeToken<List<OrderListResponseData.JoyoungOrder.Product>>() {
                }.getType());
                for(OrderListResponseData.JoyoungOrder.Product product : products){
                    List<String> serviceTypes = product.getServiceType();
                    for(String serviceType : serviceTypes){
                        B2BOrder.B2BOrderItem orderItem = new B2BOrder.B2BOrderItem();
                        orderItem.setProductName(product.getProductName());
                        orderItem.setProductCode(product.getProductCode());
                        orderItem.setProductSpec(product.getProductSpec());
                        orderItem.setClassName(product.getClassName());
                        orderItem.setServiceType(serviceType);
                        orderItem.setWarrantyType(product.getWarrantyType());
                        orderItem.setQty(product.getQty());
                        customerPo.getItems().add(orderItem);
                    }
                }
                customerPoPage.add(customerPo);
            }
            MSPage<B2BOrder> returnPage = new MSPage<>();
            returnPage.setPageNo(orderInfoPage.getPageNum());
            returnPage.setPageSize(orderInfoPage.getPageSize());
            returnPage.setPageCount(orderInfoPage.getPages());
            returnPage.setRowCount((int) orderInfoPage.getTotal());
            returnPage.setList(customerPoPage.getResult());
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        } catch (Exception e) {
            log.error("查询工单失败", e.getMessage());
            sysLogService.insert(1L,gson.toJson(workcardSearchModel),
                    "查询工单失败：" + e.getMessage(),
                    "查询工单失败", "joyoungOrderInfo/getList", "POST");
            return new MSResponse<>(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

    /**
     * 检查工单是否可以转换
     * @param orderNos
     * @return
     */
    @PostMapping("/checkWorkcardProcessFlag")
    public MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos){
        try {
            if(orderNos == null){
                return new MSResponse(new MSErrorCode(1000, "参数错误，工单编号不能为空"));
            }
            //查询出对应工单的状态
            List<JoyoungOrderInfo> orderInfos = joyoungOrderInfoService.findOrdersProcessFlag(orderNos);
            if(orderInfos == null){
                return new MSResponse(MSErrorCode.FAILURE);
            }
            for (JoyoungOrderInfo orderInfo : orderInfos) {
                if (orderInfo.getProcessFlag() != null && orderInfo.getProcessFlag() == B2BProcessFlag.PROCESS_FLAG_SUCESS.value) {
                    return new MSResponse(new MSErrorCode(1000, orderInfo.getOrderNo()+"工单已经转换成功,不能重复转换"));
                }
            }
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("检查工单失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(orderNos),"检查工单失败：" + e.getMessage(),
                    "检查工单失败","joyoungOrderInfo/checkWorkcardProcessFlag", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

    @PostMapping("/updateTransferResult")
    public MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults) {
        try {
            //根据订单号对数据分组
            Map<String,B2BOrderTransferResult> orderNoMap = workcardTransferResults.stream().collect
                    (Collectors.toMap(B2BOrderTransferResult::getB2bOrderNo,Function.identity(),(key1,key2)->key1));
            Map<Long,B2BOrderTransferResult> idMap = workcardTransferResults.stream().collect
                    (Collectors.toMap(B2BOrderTransferResult::getB2bOrderId, Function.identity(),(key1,key2)->key1));
            //查询出需要转换的工单
            List<JoyoungOrderInfo> orderInfos = joyoungOrderInfoService.findOrdersProcessFlag(workcardTransferResults);
            //存放需要转换的工单集合
            List<JoyoungOrderInfo> wis = new ArrayList<>();
            List<JoyoungOrderConfirmed> orderConfirmeds = new ArrayList<>();
            int count = 0;
            for(JoyoungOrderInfo orderInfo:orderInfos){
                //如果工单为转换成功的才存放进工单集合
                if(orderInfo.getProcessFlag() != B2BProcessFlag.PROCESS_FLAG_SUCESS.value){
                    B2BOrderTransferResult orderTransferResult = idMap.get(orderInfo.getId());
                    if(orderTransferResult == null){
                        orderTransferResult = orderNoMap.get(orderInfo.getOrderNo());
                    }
                    if(orderTransferResult != null){
                        //成功转换的才计算
                        if(orderTransferResult.getProcessFlag() == B2BProcessFlag.PROCESS_FLAG_SUCESS.value){
                            count++;
                            JoyoungOrderConfirmed joyoungOrderConfirmed = new JoyoungOrderConfirmed();
                            joyoungOrderConfirmed.setThirdSendFlag(2);
                            joyoungOrderConfirmed.setB2bOrderId(orderInfo.getId());
                            joyoungOrderConfirmed.setOrderNo(orderInfo.getOrderNo());
                            joyoungOrderConfirmed.setOrderId(orderTransferResult.getKklOrderNo());
                            orderConfirmeds.add(joyoungOrderConfirmed);
                        }
                        orderInfo.setProcessFlag(orderTransferResult.getProcessFlag());
                        orderInfo.setKklOrderId(orderTransferResult.getOrderId());
                        orderInfo.setKklOrderNo(orderTransferResult.getKklOrderNo());
                        orderInfo.setUpdateDt(orderTransferResult.getUpdateDt());
                        orderInfo.setProcessComment(orderTransferResult.getProcessComment());
                        wis.add(orderInfo);
                    }
                }
            }
            joyoungOrderInfoService.updateTransferResult(wis);
            this.orderConfirm(orderConfirmeds);
            return new MSResponse(MSErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("工单转换失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(workcardTransferResults),
                    "工单转换失败：" + e.getMessage(),
                    "工单转换失败","", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));

        }
    }

    @PostMapping("/cancelOrderTransition")
    public MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult b2BOrderTransferResult){
        try {
            joyoungOrderInfoService.cancelOrderTransition(b2BOrderTransferResult);
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("取消工单失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(b2BOrderTransferResult),"取消工单失败：" + e.getMessage(),
                    "取消工单失败","konkaOrderInfo/cancelOrderTransition", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }
}

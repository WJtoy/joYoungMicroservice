package com.kkl.kklplus.b2b.joyoung.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderConfirmed;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderInfo;
import com.kkl.kklplus.b2b.joyoung.entity.SysLog;
import com.kkl.kklplus.b2b.joyoung.http.command.OperationCommand;
import com.kkl.kklplus.b2b.joyoung.http.config.B2BTooneProperties;
import com.kkl.kklplus.b2b.joyoung.http.response.OrderListResponseData;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderInfoMapper;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BOrderMQSender;
import com.kkl.kklplus.b2b.joyoung.mq.sender.B2BWorkcardQtyDailyMQSend;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.order.B2BWorkcardQtyDaily;
import com.kkl.kklplus.entity.b2b.pb.MQB2BWorkcardQtyDailyMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BShopEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungOrderInfoService {

    @Autowired
    private B2BTooneProperties tooneProperties;

    @Autowired
    private B2BWorkcardQtyDailyMQSend b2BWorkcardQtyDailyMQSend;

    @Autowired
    private B2BOrderMQSender b2BOrderMQSender;

    @Autowired
    private SysLogService sysLogService;

    @Resource
    private JoyoungOrderInfoMapper joyoungOrderInfoMapper;

    public List<JoyoungOrderConfirmed> insertManyOrderInfo(List<OrderListResponseData.JoyoungOrder> joyoungOrders) {
        List<JoyoungOrderConfirmed> joyoungOrderConfirmeds = new ArrayList<>();
        for(OrderListResponseData.JoyoungOrder joyoungOrder : joyoungOrders) {
            //获取工单数据
            MSResponse msResponse = validationData(joyoungOrder);
            //判断所给数据是否符合条件
            if(msResponse.getCode() == MSErrorCode.SUCCESS.getCode()){
                Long id = joyoungOrderInfoMapper.findOrderInfo(joyoungOrder.getOrderNo());
                // 判断是否已加入康宝工单表，没有就添加数据
                if (id == null){
                    //填充数据
                    JoyoungOrderInfo newOrderInfo = transferOrder(joyoungOrder);
                    try{
                        //保存数据
                        joyoungOrderInfoMapper.insert(newOrderInfo);
                        if(tooneProperties.getDataSourceConfig().getOrderMqEnabled()) {
                            MQB2BOrderMessage.B2BOrderMessage.Builder builder = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                                    .setId(newOrderInfo.getId())
                                    .setDataSource(B2BDataSourceEnum.JOYOUNG.id)
                                    .setOrderNo(newOrderInfo.getOrderNo())
                                    .setParentBizOrderId(newOrderInfo.getOrderNo())
                                    .setShopId(B2BShopEnum.JOYOUNG.id)
                                    .setUserName(newOrderInfo.getUserName())
                                    .setUserMobile(newOrderInfo.getUserMobile())
                                    .setUserAddress(newOrderInfo.getUserAddress())
                                    .setBrand(newOrderInfo.getBrand())
                                    .setStatus(1)
                                    .setIssueBy(newOrderInfo.getIssueBy())
                                    .setDescription(newOrderInfo.getRemarks())
                                    .setRemarks(newOrderInfo.getRemarks())
                                    .setQuarter(newOrderInfo.getQuarter());
                            for (OrderListResponseData.JoyoungOrder.Product product : newOrderInfo.getItems()) {
                                for (String serviceType : product.getServiceType()) {
                                    MQB2BOrderMessage.B2BOrderItem b2BOrderItem = MQB2BOrderMessage.B2BOrderItem.newBuilder()
                                            .setProductCode(product.getProductCode())
                                            .setProductName(product.getProductName() != null ? product.getProductName() : "")
                                            .setProductSpec(product.getProductSpec() != null ? product.getProductSpec() : "")
                                            .setServiceType(serviceType)
                                            .setWarrantyType(product.getWarrantyType())
                                            .setQty(product.getQty())
                                            .build();
                                    builder.addB2BOrderItem(b2BOrderItem);
                                }
                            }
                            MQB2BOrderMessage.B2BOrderMessage b2BOrderMessage = builder.build();
                            //调用转单队列
                            b2BOrderMQSender.send(b2BOrderMessage);
                        }
                    }catch (Exception e){
                        sysLogService.insert(1L,new Gson().toJson(joyoungOrder),"异常工单log添加失败：" + e.getMessage(),
                                "异常工单log添加失败", OperationCommand.OperationCode.ORDERLIST.serviceCode, "POST");
                    }
                }
            }else{
                //数据不符合就回传失败
                JoyoungOrderConfirmed joyoungOrderConfirmed = new JoyoungOrderConfirmed();
                joyoungOrderConfirmed.setOrderNo(joyoungOrder.getOrderNo());
                joyoungOrderConfirmed.setThirdSendMessage(msResponse.getMsg());
                joyoungOrderConfirmed.setThirdSendFlag(1);
                joyoungOrderConfirmeds.add(joyoungOrderConfirmed);
            }
        }
        return joyoungOrderConfirmeds;
    }

    private JoyoungOrderInfo transferOrder(OrderListResponseData.JoyoungOrder joyoungOrder) {
        JoyoungOrderInfo joyoungOrderInfo = new JoyoungOrderInfo();
        joyoungOrderInfo.setOrderNo(joyoungOrder.getOrderNo());
        joyoungOrderInfo.setUserName(joyoungOrder.getUserName());
        joyoungOrderInfo.setUserMobile(joyoungOrder.getUserMobile());
        joyoungOrderInfo.setUserPhone(joyoungOrder.getUserPhone());
        joyoungOrderInfo.setUserProvince(joyoungOrder.getUserProvince());
        joyoungOrderInfo.setUserCity(joyoungOrder.getUserCity());
        joyoungOrderInfo.setUserCounty(joyoungOrder.getUserCounty());
        joyoungOrderInfo.setUserStreet(joyoungOrder.getUserStreet());
        joyoungOrderInfo.setUserAddress(joyoungOrder.getUserAddress());
        joyoungOrderInfo.setShopId(joyoungOrder.getShopId());
        joyoungOrderInfo.setBrand(joyoungOrder.getBrand());
        joyoungOrderInfo.setReceiveDate(joyoungOrder.getReceiveDate());
        String description = joyoungOrder.getDescription();
        joyoungOrderInfo.setDescription(description != null ? StringUtils.left(joyoungOrder.getDescription(),200):"");
        String remarks = joyoungOrder.getRemarks();
        joyoungOrderInfo.setRemarks(remarks != null ? StringUtils.left(joyoungOrder.getRemarks(),200):"");
        joyoungOrderInfo.setStatus(joyoungOrder.getStatus());
        String issueBy = joyoungOrder.getIssueBy();
        joyoungOrderInfo.setIssueBy(issueBy != null ? StringUtils.left(joyoungOrder.getIssueBy(),20):"");
        joyoungOrderInfo.setItems(joyoungOrder.getItems());
        joyoungOrderInfo.setItemsJson(new Gson().toJson(joyoungOrder.getItems()));
        joyoungOrderInfo.preInsert();
        joyoungOrderInfo.setCreateById(1L);
        joyoungOrderInfo.setUpdateById(1L);
        joyoungOrderInfo.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderInfo.setProcessTime(0);
        joyoungOrderInfo.setQuarter(QuarterUtils.getQuarter(joyoungOrderInfo.getCreateDate()));
        return joyoungOrderInfo;
    }

    public MSResponse validationData(OrderListResponseData.JoyoungOrder joyoungOrder) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(joyoungOrder == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("工单资料不能为空！");
            return msResponse;
        }
        String orderNo = joyoungOrder.getOrderNo();
        if(StringUtils.isBlank(orderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("工单编号orderNo不能为空！");
            return msResponse;
        }
        /*String shopId = joyoungOrder.getShopId();
        if(StringUtils.isBlank(shopId)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("商铺shopId不能为空！");
            return msResponse;
        }*/
        String userName = joyoungOrder.getUserName();
        if(StringUtils.isBlank(userName)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("买家姓名userName不能为空！");
            return msResponse;
        }
        String userMobile = joyoungOrder.getUserMobile();
        String userPhone = joyoungOrder.getUserPhone();
        if(StringUtils.isBlank(userMobile) && StringUtils.isBlank(userPhone)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("买家电话userMobile或userPhone至少一个！");
            return msResponse;
        }
        String userAddress = joyoungOrder.getUserAddress();
        if(StringUtils.isBlank(userAddress)){
            msResponse.setMsg("详细地址userAddress不能为空！");
            return msResponse;
        }
        List<OrderListResponseData.JoyoungOrder.Product> items = joyoungOrder.getItems();
        if(items == null || items.size() <= 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("产品集合items不能为空！");
        }
        boolean itemFlag = false;
        for(OrderListResponseData.JoyoungOrder.Product item : items){
            String itemCode = item.getProductCode();
            String itemName = item.getProductName();
            Integer qty = item.getQty();
            List<String> serviceTypes = item.getServiceType();
            if(serviceTypes != null && serviceTypes.size() > 0) {
                for (String serviceType : serviceTypes) {
                    if (StringUtils.isBlank(serviceType)) {
                        msResponse.setErrorCode(MSErrorCode.FAILURE);
                        msResponse.setMsg("产品服务类型serviceType不能为空！");
                        return msResponse;
                    }
                }
            }else{
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("产品服务类型serviceType不能为空！");
            }
            String warrantyType = item.getWarrantyType();
            if(StringUtils.isBlank(warrantyType)){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("产品质保类型warrantyType不能为空！");
                return msResponse;
            }
            if(StringUtils.isBlank(itemCode) || StringUtils.isBlank(itemName)||
                    qty == null || qty <=0){
                itemFlag = true;
            }
        }
        if(itemFlag){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("产品数据错误！");
        }
        return msResponse;
    }

    public Page<JoyoungOrderInfo> getList(B2BOrderSearchModel workcardSearchModel) {
        if (workcardSearchModel.getPage() != null) {
            PageHelper.startPage(workcardSearchModel.getPage().getPageNo(), workcardSearchModel.getPage().getPageSize());
            return joyoungOrderInfoMapper.getList(workcardSearchModel);
        }else {
            return null;
        }

    }

    public List<JoyoungOrderInfo> findOrdersProcessFlag(List<B2BOrderTransferResult> orderNos) {
        List<Long> ids = new ArrayList<>();
        for(B2BOrderTransferResult transferResult : orderNos){
            Long id = transferResult.getB2bOrderId();
            if(id != null && id > 0){
                ids.add(id);
            }else{
                return joyoungOrderInfoMapper.findOrdersProcessFlag(orderNos);
            }
        }
        return joyoungOrderInfoMapper.findOrdersByIds(ids);
    }

    @Transactional
    public void updateTransferResult(List<JoyoungOrderInfo> wis) {
        for(JoyoungOrderInfo orderInfo : wis){
            orderInfo.preUpdate();
            joyoungOrderInfoMapper.updateTransferResult(orderInfo);
        }
    }

    public void cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult) {
        joyoungOrderInfoMapper.cancelOrderTransition(b2BOrderTransferResult);
    }

    public JoyoungOrderInfo findOrderByOrderNo(String orderNo) {
        return joyoungOrderInfoMapper.findOrderByOrderNo(orderNo);
    }
}

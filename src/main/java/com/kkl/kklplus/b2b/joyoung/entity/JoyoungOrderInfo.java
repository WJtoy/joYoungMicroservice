package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.b2b.joyoung.http.response.OrderListResponseData;
import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

import java.util.List;

@Data
public class JoyoungOrderInfo extends B2BBase<JoyoungOrderInfo>{

    private Long kklOrderId;
    private String kklOrderNo;
    private String orderNo;
    private String userName;
    private String userMobile;
    private String userPhone;
    private String userProvince;
    private String userCity;
    private String userCounty;
    private String userStreet;
    private String userAddress;
    private String shopId;
    private String brand;
    private Long receiveDate;
    private String description;
    private String remarks;
    private Long status;
    private String issueBy;
    private List<OrderListResponseData.JoyoungOrder.Product> items;
    private String itemsJson;

}

package com.kkl.kklplus.b2b.joyoung.http.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderListResponseData implements Serializable{

    private Integer code;

    private String msg;

    private List<JoyoungOrder> data = Lists.newArrayList();

    @Data
    public static class JoyoungOrder implements Serializable{

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
        private List<Product> items = Lists.newArrayList();

        @Data
        public static class Product implements Serializable{
            private String productCode;
            private String productName;
            private String productSpec;
            private String className;
            private Integer qty;
            private List<String> serviceType;
            private String warrantyType;
        }

    }
}

package com.kkl.kklplus.b2b.joyoung.entity;

import com.google.common.collect.Lists;
import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JoyoungOrderCompleted extends B2BBase<JoyoungOrderCompleted>{
    /**
     * 必填，工单号
     */
    private String orderNo = "";

    /**
     * 可选，完工备注
     */
    private String finishNote;

    /**
     * 必填，工单产品明细集合
     */
    private List<ProductDetail> items = Lists.newArrayList();

    private String itemsJson;

    @Data
    public static class ProductDetail implements Serializable {

        /**
         * 必填，产品编码
         */
        private String itemCode = "";

        /**
         * 必填，配件图片
         */
        private String pic1 = "";

        /**
         * 可选，安装卡图片
         */
        private String pic2;

        /**
         * 可选，现场图片
         */
        private String pic3;

        /**
         * 可选，条码图片
         */
        private String pic4;

        /**
         * 可选，完工条码
         */
        private String barcode;

        /**
         * 可选，外机完工条码
         */
        private String outBarcode;

        private List<String> serviceItems;

        private String remark;

        private List<Phenomenon> pmCodeList;

        @Data
        public static class Phenomenon implements Serializable {

            private String pmCode;
            private List<Reason> reasonCodeList;

            @Data
            public static class Reason implements Serializable{
                private String reasonCode;
                private List<String> stepCode;
            }
        }

    }

}

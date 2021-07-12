package com.kkl.kklplus.b2b.joyoung.http.request;

import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.google.common.collect.Lists;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建配件单RequestParam
 */
@Data
public class MaterialRequestParam extends RequestParam {

    /**
     * 快可立配件单号
     */
    private String kklMasterNo;
    /**
     * 快可立工单号
     */
    private String kklOrderNo;
    /**
     * 九阳工单号
     */
    private String orderNo;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户电话
     */
    private String userMobile;
    /**
     * 用户地址
     */
    private String userAddress;
    /**
     * 配件单图片
     */
    private List<String> pics = Lists.newArrayList();
    /**
     * 同个工单配件单次数
     */
    private Long masterCount;
    /**
     * 故障描述
     */
    private String description;

    private Integer returnFlag;
    /**
     * 配件集合MaterItem
     */
    private List<MaterItem> materItems = Lists.newArrayList();

    @Data
    public static class MaterItem implements Serializable {
        private String materialDesc;
        private int qty;
        private String productName;
    }
}

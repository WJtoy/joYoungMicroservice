package com.kkl.kklplus.b2b.joyoung.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

/**
 *配件发货消息实体
 * @author chenxj
 * @date 2019/08/20
 */
@Data
public class JoyoungMaterialDeliver extends B2BBase<JoyoungMaterialDeliver>{
    /**
     * 快可立配件单号
     */
    private String kklMasterNo;
    /**
     * 快可立配件单ID
     */
    private Long kklMasterId;
    /**
     * 快递公司
     */
    private String expressCompany;
    /**
     * 快递单号
     */
    private String expressNo;
    /**
     * 发货时间
     */
    private String deliveryDate;
    /**
     * 备注
     */
    private String remark;

    /**
     * 发货时间 长整形
     */
    private Long deliveryDt;

}

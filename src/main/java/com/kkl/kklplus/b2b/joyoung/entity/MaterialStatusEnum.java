package com.kkl.kklplus.b2b.joyoung.entity;

/**
 *配件单状态枚举
 * @author chenxj
 * @date 2019/09/06
 */
public enum MaterialStatusEnum {

    NONE(0, "无"),
    NEW(1, "待审核"),
    APPROVED(2, "待发货"),
    SENDED(3, "已发货"),
    CLOSED(4, "已关闭"),
    REJECT(5, "已驳回"),
    ARRIVAL(6, "已到货");

    public int value;
    public String label;

    private MaterialStatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }
}

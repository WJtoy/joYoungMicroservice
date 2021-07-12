package com.kkl.kklplus.b2b.joyoung.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderInfo;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JoyoungOrderInfoMapper {

    Long findOrderInfo(@Param("orderNo") String orderNo);

    void insert(JoyoungOrderInfo newOrderInfo);

    Page<JoyoungOrderInfo> getList(B2BOrderSearchModel workcardSearchModel);

    List<JoyoungOrderInfo> findOrdersProcessFlag(@Param("orderNos") List<B2BOrderTransferResult> orderNos);

    void updateTransferResult(JoyoungOrderInfo orderInfo);

    void cancelOrderTransition(B2BOrderTransferResult b2BOrderTransferResult);

    String findOrderNoByKklOrderId(@Param("orderId") Long orderId);

    /**
     * 根据ID集合获取工单
     * @param ids
     * @return
     */
    List<JoyoungOrderInfo> findOrdersByIds(@Param("ids") List<Long> ids);

    /**
     * 根据工单号查询工单信息
     * @param orderNo
     * @return
     */
    JoyoungOrderInfo findOrderByOrderNo(@Param("orderNo") String orderNo);
}

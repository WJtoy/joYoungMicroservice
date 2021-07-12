package com.kkl.kklplus.b2b.joyoung.mapper;


import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *九阳咨询单
 * @author chenxj
 * @date 2019/09/27
 */
@Mapper
public interface JoyoungConsultingOrderMapper {
    /**
     * 新增咨询单
     * @param consultingOrder
     */
    void insert(JoyoungConsultingOrder consultingOrder);

    /**
     * 根据单号获取咨询单
     * @param consultingNo
     * @return
     */
    JoyoungConsultingOrder findConsultingOrderByConsultingNo(@Param("consultingNo") String consultingNo);

    /**
     * 更新咨询单处理标志
     * @param id
     * @param processFlag
     * @param processComment
     */
    void updateProcessFlag(@Param("id") Long id,
                           @Param("kklConsultingId") Long kklConsultingId,
                           @Param("processFlag") Integer processFlag,
                           @Param("processComment") String processComment);

}

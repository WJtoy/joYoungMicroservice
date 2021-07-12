package com.kkl.kklplus.b2b.joyoung.mapper;


import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderLog;
import org.apache.ibatis.annotations.Mapper;

/**
 *咨询单跟踪日志
 * @author chenxj
 * @date 2019/09/27
 */
@Mapper
public interface JoyoungConsultingOrderLogMapper {
    /**
     * 新增日志
     * @param consultingOrderLog
     */
    void insert(JoyoungConsultingOrderLog consultingOrderLog);
}

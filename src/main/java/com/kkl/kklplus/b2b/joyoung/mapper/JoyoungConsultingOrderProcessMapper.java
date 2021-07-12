package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.b2b.joyoung.entity.JoyoungConsultingOrderProcess;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface JoyoungConsultingOrderProcessMapper {
    
    void updateProcessFlag(JoyoungConsultingOrderProcess consultingOrderProcess);

    void insert(JoyoungConsultingOrderProcess consultingOrderProcess);
}

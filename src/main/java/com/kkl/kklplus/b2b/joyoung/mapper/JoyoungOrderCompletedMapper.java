package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCompleted;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoyoungOrderCompletedMapper {
    void insert(JoyoungOrderCompleted joyoungOrderCompleted);

    void updateProcessFlag(JoyoungOrderCompleted joyoungOrderCompleted);
}

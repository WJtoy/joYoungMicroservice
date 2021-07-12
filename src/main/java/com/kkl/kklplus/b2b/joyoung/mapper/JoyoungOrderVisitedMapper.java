package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderVisited;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoyoungOrderVisitedMapper {

    void insert(JoyoungOrderVisited joyoungOrderVisited);

    void updateProcessFlag(JoyoungOrderVisited joyoungOrderVisited);
}

package com.kkl.kklplus.b2b.joyoung.mapper;


import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderProcessLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JoyoungOrderProcesslogMapper {

    void insert(JoyoungOrderProcessLog orderProcessLog);

    void updateProcessFlag(JoyoungOrderProcessLog orderProcessLog);

}

package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderConfirmed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JoyoungOrderConfirmedMapper {
    void insert(JoyoungOrderConfirmed joyoungOrderConfirmed);

    void updateProcessFlag(@Param("joyoungOrderConfirmeds")List<JoyoungOrderConfirmed> joyoungOrderConfirmeds,
                           @Param("processFlag")Integer processFlag,
                           @Param("processComment")String processComment,
                           @Param("updateDate") Long updateDate);
}

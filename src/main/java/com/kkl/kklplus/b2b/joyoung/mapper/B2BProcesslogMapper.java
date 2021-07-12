package com.kkl.kklplus.b2b.joyoung.mapper;


import com.github.pagehelper.Page;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface B2BProcesslogMapper {

    Integer insert(B2BOrderProcesslog b2BProcesslog);

    void updateProcessFlag(B2BOrderProcesslog b2BProcesslog);

    Page<B2BOrderProcesslog> getList(
            @Param("processLogSearchModel") B2BProcessLogSearchModel processLogSearchModel,
            @Param("code") String code);
}

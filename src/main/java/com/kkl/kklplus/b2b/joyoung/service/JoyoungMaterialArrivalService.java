package com.kkl.kklplus.b2b.joyoung.service;

import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungMaterialArrivalMapper;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.common.material.B2BMaterialArrival;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialArrivalService {

    @Resource
    private JoyoungMaterialArrivalMapper joyoungMaterialArrivalMapper;

    /**
     * 插入数据
     * @param materialArrival
     */
    public void insert(B2BMaterialArrival materialArrival){
        materialArrival.preInsert();
        materialArrival.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        materialArrival.setProcessTime(0);
        materialArrival.setQuarter(QuarterUtils.getQuarter(materialArrival.getCreateDate()));
        joyoungMaterialArrivalMapper.insert(materialArrival);
    }

    /**
     * 修改数据
     * @param materialArrival
     */
    public void updateProcessFlag(B2BMaterialArrival materialArrival){
        materialArrival.preUpdate();
        materialArrival.setProcessComment(StringUtils.left(materialArrival.getProcessComment(),200));
        joyoungMaterialArrivalMapper.updateProcessFlag(materialArrival);
    }

}

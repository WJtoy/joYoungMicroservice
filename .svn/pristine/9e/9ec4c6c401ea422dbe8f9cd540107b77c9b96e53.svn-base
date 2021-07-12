package com.kkl.kklplus.b2b.joyoung.service;

import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderPlanned;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderPlannedMapper;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
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
public class JoyoungOrderPlannedService {


    @Resource
    private JoyoungOrderPlannedMapper joyoungOrderPlannedMapper;

    public void insert(JoyoungOrderPlanned joyoungOrderPlanned) {
        joyoungOrderPlanned.preInsert();
        joyoungOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderPlanned.setProcessTime(0);
        joyoungOrderPlanned.setQuarter(QuarterUtils.getQuarter(joyoungOrderPlanned.getCreateDate()));
        joyoungOrderPlannedMapper.insert(joyoungOrderPlanned);
    }

    public void updateProcessFlag(JoyoungOrderPlanned joyoungOrderPlanned) {
        joyoungOrderPlanned.preUpdate();
        joyoungOrderPlanned.setProcessComment(StringUtils.left(joyoungOrderPlanned.getProcessComment(), 200));
        joyoungOrderPlannedMapper.updateProcessFlag(joyoungOrderPlanned);
    }
}

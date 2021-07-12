package com.kkl.kklplus.b2b.joyoung.service;

import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderVisited;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderVisitedMapper;
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
public class JoyoungOrderVisitedService {

    @Resource
    private JoyoungOrderVisitedMapper joyoungOrderVisitedMapper;

    public void insert(JoyoungOrderVisited joyoungOrderVisited) {
        joyoungOrderVisited.preInsert();
        joyoungOrderVisited.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderVisited.setProcessTime(0);
        joyoungOrderVisited.setQuarter(QuarterUtils.getQuarter(joyoungOrderVisited.getCreateDate()));
        joyoungOrderVisitedMapper.insert(joyoungOrderVisited);
    }

    public void updateProcessFlag(JoyoungOrderVisited joyoungOrderVisited) {
        joyoungOrderVisited.preUpdate();
        joyoungOrderVisited.setProcessComment(StringUtils.left(joyoungOrderVisited.getProcessComment(),200));
        joyoungOrderVisitedMapper.updateProcessFlag(joyoungOrderVisited);
    }
}

package com.kkl.kklplus.b2b.joyoung.service;

import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCancelled;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderCancelledMapper;
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
public class JoyoungOrderCancelledService {

    @Resource
    private JoyoungOrderCancelledMapper joyoungOrderCancelledMapper;

    public void insert(JoyoungOrderCancelled joyoungOrderCancelled) {
        joyoungOrderCancelled.preInsert();
        joyoungOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderCancelled.setProcessTime(0);
        joyoungOrderCancelled.setQuarter(QuarterUtils.getQuarter(joyoungOrderCancelled.getCreateDate()));
        joyoungOrderCancelledMapper.insert(joyoungOrderCancelled);
    }

    public void updateProcessFlag(JoyoungOrderCancelled joyoungOrderCancelled) {
        joyoungOrderCancelled.preUpdate();
        joyoungOrderCancelled.setProcessComment(StringUtils.left(joyoungOrderCancelled.getProcessComment(),200));
        joyoungOrderCancelledMapper.updateProcessFlag(joyoungOrderCancelled);
    }
}

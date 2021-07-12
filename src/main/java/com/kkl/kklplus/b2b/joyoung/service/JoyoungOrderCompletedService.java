package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.entity.joyoung.sd.JoyoungOrderCompleted;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderCompletedMapper;
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
public class JoyoungOrderCompletedService {

    @Resource
    private JoyoungOrderCompletedMapper joyoungOrderCompletedMapper;


    public void insert(JoyoungOrderCompleted joyoungOrderCompleted) {
        joyoungOrderCompleted.setItemsJson(new Gson().toJson(joyoungOrderCompleted.getItems()));
        joyoungOrderCompleted.preInsert();
        joyoungOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        joyoungOrderCompleted.setProcessTime(0);
        joyoungOrderCompleted.setQuarter(QuarterUtils.getQuarter(joyoungOrderCompleted.getCreateDate()));
        joyoungOrderCompletedMapper.insert(joyoungOrderCompleted);
    }

    public void updateProcessFlag(JoyoungOrderCompleted joyoungOrderCompleted) {
        joyoungOrderCompleted.preUpdate();
        joyoungOrderCompleted.setProcessComment(StringUtils.left(joyoungOrderCompleted.getProcessComment(),200));
        joyoungOrderCompletedMapper.updateProcessFlag(joyoungOrderCompleted);
    }
}

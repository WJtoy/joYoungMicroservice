package com.kkl.kklplus.b2b.joyoung.service;

import com.kkl.kklplus.b2b.joyoung.entity.JoyoungOrderConfirmed;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungOrderConfirmedMapper;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungOrderConfirmedService {

    @Resource
    private JoyoungOrderConfirmedMapper joyoungOrderConfirmedMapper;

    @Transactional
    public void insert(List<JoyoungOrderConfirmed> joyoungOrderConfirmeds) {
        for(JoyoungOrderConfirmed joyoungOrderConfirmed : joyoungOrderConfirmeds) {
            joyoungOrderConfirmed.setResultFlag(0);
            joyoungOrderConfirmed.setCreateById(1L);
            joyoungOrderConfirmed.setUpdateById(1L);
            joyoungOrderConfirmed.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
            joyoungOrderConfirmed.setProcessTime(0);
            joyoungOrderConfirmed.preInsert();
            joyoungOrderConfirmed.setQuarter(QuarterUtils.getQuarter(joyoungOrderConfirmed.getCreateDate()));
            joyoungOrderConfirmedMapper.insert(joyoungOrderConfirmed);
        }
    }

    public void updateProcessFlag(List<JoyoungOrderConfirmed> joyoungOrderConfirmeds, int processFlag, String processComment, long updateDate) {
        joyoungOrderConfirmedMapper.updateProcessFlag(joyoungOrderConfirmeds,
                processFlag,processComment,updateDate);
    }
}

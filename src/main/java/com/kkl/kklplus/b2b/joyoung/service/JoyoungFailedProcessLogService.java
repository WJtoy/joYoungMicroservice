package com.kkl.kklplus.b2b.joyoung.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.kkl.kklplus.b2b.joyoung.entity.FailedProcessLog;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungFailedProcesslogMapper;
import com.kkl.kklplus.entity.b2bcenter.md.B2BInterfaceIdEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 失败日志操作
 * @author chenxj
 * @date 2019/07/17
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungFailedProcessLogService {

    @Resource
    private JoyoungFailedProcesslogMapper joyoungFailedProcesslogMapper;

    /**
     * 记录失败记录
     * @param processlog
     * @param id
     */
    public void insertOrUpdateFailedLog(B2BOrderProcesslog processlog,Long id,Long b2bOrderId){
        try {
            if(id != null && id > 0) {
                Long logId = joyoungFailedProcesslogMapper.getFailedLogById(id);
                String processComment = processlog.getProcessComment();
                processComment = StringUtils.left(processComment, 250);
                if (logId != null && logId > 0) {
                    //更新处理次数
                    joyoungFailedProcesslogMapper.updateFailedLogTimes(logId, processComment);
                } else {
                    FailedProcessLog failedLog = new FailedProcessLog();
                    if(b2bOrderId != null){
                        failedLog.setB2bOrderId(b2bOrderId);
                    }
                    failedLog.setId(id);
                    failedLog.setDataSource(processlog.getDataSource());
                    failedLog.setInfoJson(processlog.getInfoJson());
                    failedLog.setResultJson(processlog.getResultJson());
                    failedLog.setInterfaceName(processlog.getInterfaceName());
                    failedLog.setProcessComment(processComment);
                    failedLog.setCreateById(processlog.getCreateById());
                    failedLog.setUpdateById(processlog.getUpdateById());
                    failedLog.setCreateDt(processlog.getCreateDt());
                    failedLog.setUpdateDt(processlog.getUpdateDt());
                    failedLog.setQuarter(processlog.getQuarter());
                    joyoungFailedProcesslogMapper.insert(failedLog);
                }
            }
        }catch (Exception e){
            log.error("失败日志记录失败！{}:{}",processlog.toString(),e.getMessage());

        }
    }

    public MSPage<B2BOrderProcesslog> getList(B2BProcessLogSearchModel processLogSearchModel, String code) {
        if (processLogSearchModel.getPage() != null) {
            PageHelper.startPage(processLogSearchModel.getPage().getPageNo(), processLogSearchModel.getPage().getPageSize());
            Page<B2BOrderProcesslog> list = joyoungFailedProcesslogMapper.getList(processLogSearchModel,code);
            for(B2BOrderProcesslog log : list){
                log.setInterfaceName(
                        B2BInterfaceIdEnum.getByCode(log.getInterfaceName()).description);
            }
            MSPage<B2BOrderProcesslog> returnPage = new MSPage<>();
            returnPage.setPageNo(list.getPageNum());
            returnPage.setPageSize(list.getPageSize());
            returnPage.setPageCount(list.getPages());
            returnPage.setRowCount((int) list.getTotal());
            returnPage.setList(list);
            return returnPage;
        } else {
            return null;
        }
    }

    public B2BOrderProcesslog getLogById(Long id) {
        return joyoungFailedProcesslogMapper.getFailedLogMessageById(id);
    }

    public void closeFailedLogById(Long processLogId) {
        joyoungFailedProcesslogMapper.closeFailedLogById(processLogId);
    }
}

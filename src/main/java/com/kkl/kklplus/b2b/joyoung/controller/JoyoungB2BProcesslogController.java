package com.kkl.kklplus.b2b.joyoung.controller;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.joyoung.service.B2BProcesslogService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BInterfaceIdEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/processlog")
public class JoyoungB2BProcesslogController {

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @PostMapping("getProcessLogList")
    public MSResponse<MSPage<B2BOrderProcesslog>> getProcessLogList(@RequestBody B2BProcessLogSearchModel processLogSearchModel){
        Page<B2BOrderProcesslog> processlogPage = b2BProcesslogService.getList(processLogSearchModel,
                B2BInterfaceIdEnum.getById(processLogSearchModel.getB2bInterfaceId()).code);
        MSPage<B2BOrderProcesslog> returnPage = new MSPage<>();
        returnPage.setPageNo(processlogPage.getPageNum());
        returnPage.setPageSize(processlogPage.getPageSize());
        returnPage.setPageCount(processlogPage.getPages());
        returnPage.setRowCount((int) processlogPage.getTotal());
        returnPage.setList(processlogPage);
        return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
    }
}

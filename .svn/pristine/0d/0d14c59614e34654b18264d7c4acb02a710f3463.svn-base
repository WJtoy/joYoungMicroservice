package com.kkl.kklplus.b2b.joyoung.service;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.joyoung.entity.MaterialStatusEnum;
import com.kkl.kklplus.b2b.joyoung.mapper.JoyoungMaterialMapper;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.common.ThreeTuple;
import com.kkl.kklplus.entity.b2b.common.TwoTuple;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import com.kkl.kklplus.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class JoyoungMaterialService {

    @Resource
    private JoyoungMaterialMapper joyoungMaterialMapper;

    /**
     * 插入数据
     * @param material
     */
    public void insert(B2BMaterial material){
        material.setMaterItemsJson(new Gson().toJson(material.getMaterItems()));
        material.setPicsJson(new Gson().toJson(material.getPics()));
        material.setStatus(MaterialStatusEnum.NEW.value);
        material.preInsert();
        material.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        material.setProcessTime(0);
        //material.setQuarter(QuarterUtils.getQuarter(material.getCreateDate()));
        joyoungMaterialMapper.insert(material);
    }

    /**
     * 修改数据
     * @param material
     */
    public void updateProcessFlag(B2BMaterial material){
        material.preUpdate();
        material.setProcessComment(StringUtils.left(material.getProcessComment(),200));
        joyoungMaterialMapper.updateProcessFlag(material);
    }

    /**
     * 按快可立配件单ID返回Id
     * @param kklMasterId
     */
    public Long returnIdByKklMasterId(Long kklMasterId){
        return joyoungMaterialMapper.returnIdByKklMasterId(kklMasterId);
    }

    /**
     * 根据Id修改配件单信息
     * @param material
     */
    public void updateMaterialInfo(B2BMaterial material){
        material.setMaterItemsJson(new Gson().toJson(material.getMaterItems()));
        material.setPicsJson(new Gson().toJson(material.getPics()));
        material.preUpdate();
        joyoungMaterialMapper.updateMaterialInfo(material);
    }

    /**
     * 查询B2B配件单ID，和快可立配件单ID
     */
    public ThreeTuple<Long,Long,String> findMaterialByKklMasterNo(String kklMasterNo){
        return joyoungMaterialMapper.findMaterialByKklMasterNo(kklMasterNo);
    }

    /**
     * 根据配件单ID 更新配件单状态
     * @param id
     * @param value
     * @param updateDt
     */
    public void updateStatus(Long id, int value, Long updateDt){
        joyoungMaterialMapper.updateStatus(id,value,updateDt);
    }

    public void updateProessComment(Long id, String processComment) {
        joyoungMaterialMapper.updateProcessComment(id,processComment);
    }

    public List<B2BMaterial> findMaterialsByKklOrderId(Long orderId) {
        return joyoungMaterialMapper.findMaterialsByKklOrderId(orderId);
    }

    public B2BMaterial findMaterialByKklMasterId(Long kklMasterId) {
        return joyoungMaterialMapper.findMaterialByKklMasterId(kklMasterId);
    }
}

package com.kkl.kklplus.b2b.joyoung.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.joyoung.entity.B2BMaterialSearchModel;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *审核配件单记录表数据操作
 * @author chenxj
 * @date 2019/08/19
 */
@Mapper
public interface JoyoungMaterialApplyMapper {
    /**
     * 根据快可立配件单号查询审核信息
     * @param kklMasterNo
     * @return
     */
    Long findMaterialApply(@Param("kklMasterNo") String kklMasterNo);

    /**
     * 新增审核配件单操作记录
     * @param joyoungMaterialApply
     */
    void insert(JoyoungMaterialApply joyoungMaterialApply);

    /**
     * 根据多个ID查询审核信息
     * @param ids
     * @return
     */
    List<JoyoungMaterialApply> findMaterialApplyById(@Param("ids") List<Long> ids);

    /**
     * 更新消息的处理结果
     * @param id
     * @param processFlag
     * @param processComment
     */
    void updateProcessFlag(@Param("id") Long id,
                           @Param("processFlag") Integer processFlag,
                           @Param("processComment") String processComment);

    /**
     * 获取未处理的审核消息列表
     * @param materialSearchModel
     * @return
     */
    Page<JoyoungMaterialApply> getList(B2BMaterialSearchModel materialSearchModel);
}

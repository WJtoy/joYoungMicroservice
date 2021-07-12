package com.kkl.kklplus.b2b.joyoung.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.joyoung.entity.B2BMaterialSearchModel;
import com.kkl.kklplus.b2b.joyoung.entity.JoyoungMaterialDeliver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *配件发货消息
 * @author chenxj
 * @date 2019/08/21
 */
@Mapper
public interface JoyoungMaterialDeliverMapper {
    /**
     * 新增配件单发货信息
     * @param materialDeliver
     */
    void insert(JoyoungMaterialDeliver materialDeliver);

    /**
     * 根据配件单号，查询所有的发货信息
     * @param kklMasterNo
     * @return
     */
    List<String> findDeliverByMaterNo(@Param("kklMasterNo") String kklMasterNo);

    /**
     * 查询发货信息
     * @param ids
     * @return
     */
    List<JoyoungMaterialDeliver> findMaterialDeliverById(@Param("ids") List<Long> ids);

    /**
     * 更新发货消息处理状态
     * @param id
     * @param processFlag
     * @param processComment
     */
    void updateProcessFlag(@Param("id") Long id,
                           @Param("processFlag") Integer processFlag,
                           @Param("processComment") String processComment);

    /**
     * 发货消息列表
     * @param materialSearchModel
     * @return
     */
    Page<JoyoungMaterialDeliver> getList(B2BMaterialSearchModel materialSearchModel);

}

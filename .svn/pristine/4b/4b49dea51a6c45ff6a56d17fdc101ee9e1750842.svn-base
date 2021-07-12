package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.entity.b2b.common.ThreeTuple;
import com.kkl.kklplus.entity.b2b.common.TwoTuple;
import com.kkl.kklplus.entity.common.material.B2BMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 创建配件单Dao
 */
@Mapper
public interface JoyoungMaterialMapper {

    /**
     * 插入数据
     * @param material
     */
    void insert(B2BMaterial material);

    /**
     * 修改数据
     * @param material
     */
    void updateProcessFlag(B2BMaterial material);

    /**
     * 按快可立配件单ID返回Id
     * @param kklMasterId
     * @return
     */
    Long returnIdByKklMasterId(@Param("kklMasterId") Long kklMasterId);

    /**
     * 根据Id修改配件单信息
     * @param material
     */
    void updateMaterialInfo(B2BMaterial material);
    /**
     * 根据配件单号查询配件单ID
     * aElement:id  bElement:快可立配件ID cElement:分片
     * @param kklMasterNo
     * @return
     */
    ThreeTuple<Long,Long,String> findMaterialByKklMasterNo(@Param("kklMasterNo")String kklMasterNo);

    /**
     * 更新配件状态
     * @param id
     * @param updateDt
     */
    void updateStatus(@Param("id") Long id, @Param("status") int status, @Param("updateDt")Long updateDt);

    void updateProcessComment(@Param("id") Long id,
                              @Param("processComment") String processComment);

    /**
     * 根据工单ID查询多个配件单
     * @param orderId
     * @return
     */
    List<B2BMaterial> findMaterialsByKklOrderId(@Param("orderId") Long orderId);

    /**
     * 根据快可立配件单ID查询配件信息
     * @param kklMasterId
     * @return
     */
    B2BMaterial findMaterialByKklMasterId(@Param("kklMasterId")Long kklMasterId);
}

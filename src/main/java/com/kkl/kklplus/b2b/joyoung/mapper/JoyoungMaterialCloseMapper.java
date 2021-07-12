package com.kkl.kklplus.b2b.joyoung.mapper;

import com.kkl.kklplus.entity.common.material.B2BMaterialClose;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配件单关闭通知Dao
 */
@Mapper
public interface JoyoungMaterialCloseMapper {

    /**
     * 插入数据
     * @param materialClose
     */
    void insert(B2BMaterialClose materialClose);

    /**
     * 修改数据
     * @param materialClose
     */
    void updateProcessFlag(B2BMaterialClose materialClose);

}

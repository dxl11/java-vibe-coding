package com.vibe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.user.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 收货地址Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    
    /**
     * 取消其他默认地址
     * 
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE address SET is_default = 0, update_time = NOW() " +
            "WHERE user_id = #{userId} AND is_default = 1 AND is_deleted = 0")
    int cancelDefaultAddress(@Param("userId") Long userId);
    
    /**
     * 设置默认地址
     * 
     * @param id 地址ID
     * @return 更新行数
     */
    @Update("UPDATE address SET is_default = 1, update_time = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int setDefaultAddress(@Param("id") Long id);
}

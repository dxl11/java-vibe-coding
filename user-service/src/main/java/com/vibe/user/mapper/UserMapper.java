package com.vibe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

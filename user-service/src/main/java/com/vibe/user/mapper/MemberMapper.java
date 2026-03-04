package com.vibe.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.user.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 会员Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
    
    /**
     * 增加积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @return 更新行数
     */
    @Update("UPDATE member SET points = points + #{points}, total_points = total_points + #{points}, " +
            "update_time = NOW() WHERE user_id = #{userId} AND is_deleted = 0")
    int increasePoints(@Param("userId") Long userId, @Param("points") Integer points);
    
    /**
     * 消费积分
     * 
     * @param userId 用户ID
     * @param points 积分数量
     * @return 更新行数
     */
    @Update("UPDATE member SET points = points - #{points}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND points >= #{points} AND is_deleted = 0")
    int consumePoints(@Param("userId") Long userId, @Param("points") Integer points);
}

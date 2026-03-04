package com.vibe.user.service;

import com.vibe.user.dto.UserDTO;
import com.vibe.user.dto.UserLoginDTO;
import com.vibe.user.dto.UserRegisterDTO;

/**
 * 用户服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface UserService {
    
    /**
     * 用户注册
     * 
     * @param registerDTO 注册DTO
     * @return 用户DTO
     */
    UserDTO register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     * 
     * @param loginDTO 登录DTO
     * @return Token
     */
    String login(UserLoginDTO loginDTO);
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户DTO
     */
    UserDTO getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户DTO
     */
    UserDTO getUserByUsername(String username);
}

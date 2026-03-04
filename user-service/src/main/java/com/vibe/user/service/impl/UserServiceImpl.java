package com.vibe.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogMaskUtils;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.user.dto.UserDTO;
import com.vibe.user.dto.UserLoginDTO;
import com.vibe.user.dto.UserRegisterDTO;
import com.vibe.user.entity.User;
import com.vibe.user.mapper.UserMapper;
import com.vibe.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogOperation(operation = "USER_REGISTER", resource = "USER", action = "CREATE")
    @MonitorPerformance(threshold = 2000, operation = "用户注册")
    public UserDTO register(UserRegisterDTO registerDTO) {
        String maskedPhone = LogMaskUtils.maskPhone(registerDTO.getPhone());
        String maskedEmail = LogMaskUtils.maskEmail(registerDTO.getEmail());
        
        LogUtils.businessLog("USER_REGISTER", "用户注册开始", 
                registerDTO.getUsername(), maskedPhone, maskedEmail);
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername())
                .eq(User::getIsDeleted, 0);
        User existUser = userMapper.selectOne(queryWrapper);
        if (existUser != null) {
            LogUtils.businessLog("USER_REGISTER", "用户注册失败：用户名已存在", registerDTO.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (StrUtil.isNotBlank(registerDTO.getPhone())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, registerDTO.getPhone())
                    .eq(User::getIsDeleted, 0);
            existUser = userMapper.selectOne(queryWrapper);
            if (existUser != null) {
                LogUtils.businessLog("USER_REGISTER", "用户注册失败：手机号已被注册", maskedPhone);
                throw new BusinessException(400, "手机号已被注册");
            }
        }
        
        // 创建用户
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(BCrypt.hashpw(registerDTO.getPassword()))  // 密码加密
                .phone(registerDTO.getPhone())
                .email(registerDTO.getEmail())
                .nickname(StrUtil.isNotBlank(registerDTO.getNickname()) 
                        ? registerDTO.getNickname() 
                        : registerDTO.getUsername())
                .status(1)  // 默认启用
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        userMapper.insert(user);
        
        LogUtils.businessLog("USER_REGISTER", "用户注册成功", user.getId(), registerDTO.getUsername());
        
        // 转换为DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
    
    @Override
    @LogOperation(operation = "USER_LOGIN", resource = "USER", action = "QUERY")
    @MonitorPerformance(threshold = 1000, operation = "用户登录")
    public String login(UserLoginDTO loginDTO) {
        String maskedUsername = LogMaskUtils.maskObject(loginDTO.getUsername());
        LogUtils.businessLog("USER_LOGIN", "用户登录开始", maskedUsername);
        
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(User::getUsername, loginDTO.getUsername())
                .or()
                .eq(User::getPhone, loginDTO.getUsername()))
                .eq(User::getIsDeleted, 0);
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            LogUtils.businessLog("USER_LOGIN", "用户登录失败：用户不存在", maskedUsername);
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            LogUtils.businessLog("USER_LOGIN", "用户登录失败：密码错误", maskedUsername);
            throw new BusinessException(400, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            LogUtils.businessLog("USER_LOGIN", "用户登录失败：用户已被禁用", user.getId());
            throw new BusinessException(400, "用户已被禁用");
        }
        
        LogUtils.businessLog("USER_LOGIN", "用户登录成功", user.getId(), maskedUsername);
        
        // TODO: 生成JWT Token
        // 这里简化处理，实际应该使用JWT生成Token
        return "token_" + user.getId();
    }
    
    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(404, "用户不存在");
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
    
    @Override
    public UserDTO getUserByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0);
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
}

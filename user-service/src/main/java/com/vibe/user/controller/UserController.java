package com.vibe.user.controller;

import com.vibe.common.core.result.Result;
import com.vibe.user.dto.UserDTO;
import com.vibe.user.dto.UserLoginDTO;
import com.vibe.user.dto.UserRegisterDTO;
import com.vibe.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     * 
     * @param registerDTO 注册DTO
     * @return 响应结果
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        UserDTO userDTO = userService.register(registerDTO);
        return Result.success("注册成功", userDTO);
    }
    
    /**
     * 用户登录
     * 
     * @param loginDTO 登录DTO
     * @return 响应结果
     */
    @PostMapping("/login")
    public Result<String> login(@Validated @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success("登录成功", token);
    }
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return Result.success(userDTO);
    }
}

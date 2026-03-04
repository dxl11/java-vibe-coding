package com.vibe.user.controller;

import com.vibe.common.core.result.Result;
import com.vibe.user.dto.AddressDTO;
import com.vibe.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/user/address")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    /**
     * 添加收货地址
     * 
     * @param userId 用户ID
     * @param addressDTO 地址DTO
     * @return 响应结果
     */
    @PostMapping("/{userId}/add")
    public Result<AddressDTO> addAddress(
            @PathVariable Long userId,
            @Validated @RequestBody AddressDTO addressDTO) {
        AddressDTO address = addressService.addAddress(userId, addressDTO);
        return Result.success("添加收货地址成功", address);
    }
    
    /**
     * 更新收货地址
     * 
     * @param userId 用户ID
     * @param addressDTO 地址DTO
     * @return 响应结果
     */
    @PutMapping("/{userId}/update")
    public Result<AddressDTO> updateAddress(
            @PathVariable Long userId,
            @Validated @RequestBody AddressDTO addressDTO) {
        AddressDTO address = addressService.updateAddress(userId, addressDTO);
        return Result.success("更新收货地址成功", address);
    }
    
    /**
     * 删除收货地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 响应结果
     */
    @DeleteMapping("/{userId}/{addressId}")
    public Result<Boolean> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        boolean result = addressService.deleteAddress(userId, addressId);
        return Result.success("删除收货地址成功", result);
    }
    
    /**
     * 查询用户收货地址列表
     * 
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/{userId}/list")
    public Result<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
        List<AddressDTO> addresses = addressService.getUserAddresses(userId);
        return Result.success(addresses);
    }
    
    /**
     * 根据ID查询收货地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 响应结果
     */
    @GetMapping("/{userId}/{addressId}")
    public Result<AddressDTO> getAddressById(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        AddressDTO address = addressService.getAddressById(userId, addressId);
        return Result.success(address);
    }
    
    /**
     * 设置默认地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 响应结果
     */
    @PostMapping("/{userId}/{addressId}/set-default")
    public Result<Boolean> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        boolean result = addressService.setDefaultAddress(userId, addressId);
        return Result.success("设置默认地址成功", result);
    }
    
    /**
     * 获取默认地址
     * 
     * @param userId 用户ID
     * @return 响应结果
     */
    @GetMapping("/{userId}/default")
    public Result<AddressDTO> getDefaultAddress(@PathVariable Long userId) {
        AddressDTO address = addressService.getDefaultAddress(userId);
        return Result.success(address);
    }
}

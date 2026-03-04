package com.vibe.user.service;

import com.vibe.user.dto.AddressDTO;

import java.util.List;

/**
 * 收货地址服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface AddressService {
    
    /**
     * 添加收货地址
     * 
     * @param userId 用户ID
     * @param addressDTO 地址DTO
     * @return 地址DTO
     */
    AddressDTO addAddress(Long userId, AddressDTO addressDTO);
    
    /**
     * 更新收货地址
     * 
     * @param userId 用户ID
     * @param addressDTO 地址DTO
     * @return 地址DTO
     */
    AddressDTO updateAddress(Long userId, AddressDTO addressDTO);
    
    /**
     * 删除收货地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    boolean deleteAddress(Long userId, Long addressId);
    
    /**
     * 查询用户收货地址列表
     * 
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressDTO> getUserAddresses(Long userId);
    
    /**
     * 根据ID查询收货地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 地址DTO
     */
    AddressDTO getAddressById(Long userId, Long addressId);
    
    /**
     * 设置默认地址
     * 
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Long userId, Long addressId);
    
    /**
     * 获取默认地址
     * 
     * @param userId 用户ID
     * @return 地址DTO
     */
    AddressDTO getDefaultAddress(Long userId);
}

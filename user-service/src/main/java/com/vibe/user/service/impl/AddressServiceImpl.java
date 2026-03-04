package com.vibe.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.user.dto.AddressDTO;
import com.vibe.user.entity.Address;
import com.vibe.user.mapper.AddressMapper;
import com.vibe.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收货地址服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "ADDRESS_ADD", resource = "ADDRESS", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "添加收货地址")
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        LogUtils.businessLog("ADDRESS_ADD", "添加收货地址开始", userId);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() == 1) {
            addressMapper.cancelDefaultAddress(userId);
        } else {
            // 如果是第一个地址，自动设为默认
            LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Address::getUserId, userId)
                    .eq(Address::getIsDeleted, 0);
            long count = addressMapper.selectCount(queryWrapper);
            if (count == 0) {
                addressDTO.setIsDefault(1);
            }
        }
        
        Address address = Address.builder()
                .userId(userId)
                .receiverName(addressDTO.getReceiverName())
                .receiverPhone(addressDTO.getReceiverPhone())
                .province(addressDTO.getProvince())
                .city(addressDTO.getCity())
                .district(addressDTO.getDistrict())
                .detailAddress(addressDTO.getDetailAddress())
                .postalCode(addressDTO.getPostalCode())
                .isDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : 0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        addressMapper.insert(address);
        
        LogUtils.businessLog("ADDRESS_ADD", "添加收货地址成功", address.getId());
        
        return convertToDTO(address);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "ADDRESS_UPDATE", resource = "ADDRESS", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "更新收货地址")
    public AddressDTO updateAddress(Long userId, AddressDTO addressDTO) {
        LogUtils.businessLog("ADDRESS_UPDATE", "更新收货地址开始", userId, addressDTO.getId());
        
        Address address = addressMapper.selectById(addressDTO.getId());
        if (address == null || address.getIsDeleted() == 1) {
            throw new BusinessException(404, "收货地址不存在");
        }
        
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() == 1) {
            addressMapper.cancelDefaultAddress(userId);
        }
        
        // 更新字段
        if (addressDTO.getReceiverName() != null) {
            address.setReceiverName(addressDTO.getReceiverName());
        }
        if (addressDTO.getReceiverPhone() != null) {
            address.setReceiverPhone(addressDTO.getReceiverPhone());
        }
        if (addressDTO.getProvince() != null) {
            address.setProvince(addressDTO.getProvince());
        }
        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }
        if (addressDTO.getDistrict() != null) {
            address.setDistrict(addressDTO.getDistrict());
        }
        if (addressDTO.getDetailAddress() != null) {
            address.setDetailAddress(addressDTO.getDetailAddress());
        }
        if (addressDTO.getPostalCode() != null) {
            address.setPostalCode(addressDTO.getPostalCode());
        }
        if (addressDTO.getIsDefault() != null) {
            address.setIsDefault(addressDTO.getIsDefault());
        }
        address.setUpdateTime(LocalDateTime.now());
        
        addressMapper.updateById(address);
        
        LogUtils.businessLog("ADDRESS_UPDATE", "更新收货地址成功", address.getId());
        
        return convertToDTO(address);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "ADDRESS_DELETE", resource = "ADDRESS", action = "DELETE")
    public boolean deleteAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getIsDeleted() == 1) {
            throw new BusinessException(404, "收货地址不存在");
        }
        
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }
        
        address.setIsDeleted(1);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
        
        LogUtils.businessLog("ADDRESS_DELETE", "删除收货地址成功", addressId);
        return true;
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "收货地址查询")
    public List<AddressDTO> getUserAddresses(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDeleted, 0)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime);
        
        List<Address> addresses = addressMapper.selectList(queryWrapper);
        
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "收货地址查询")
    public AddressDTO getAddressById(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getIsDeleted() == 1) {
            throw new BusinessException(404, "收货地址不存在");
        }
        
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }
        
        return convertToDTO(address);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "ADDRESS_SET_DEFAULT", resource = "ADDRESS", action = "UPDATE")
    public boolean setDefaultAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || address.getIsDeleted() == 1) {
            throw new BusinessException(404, "收货地址不存在");
        }
        
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }
        
        // 取消其他默认地址
        addressMapper.cancelDefaultAddress(userId);
        
        // 设置当前地址为默认
        addressMapper.setDefaultAddress(addressId);
        
        LogUtils.businessLog("ADDRESS_SET_DEFAULT", "设置默认地址成功", addressId);
        return true;
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "默认地址查询")
    public AddressDTO getDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .eq(Address::getIsDeleted, 0)
                .orderByDesc(Address::getUpdateTime)
                .last("LIMIT 1");
        
        Address address = addressMapper.selectOne(queryWrapper);
        if (address == null) {
            throw new BusinessException(404, "默认地址不存在");
        }
        
        return convertToDTO(address);
    }
    
    /**
     * 转换为DTO
     * 
     * @param address 地址实体
     * @return 地址DTO
     */
    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        BeanUtils.copyProperties(address, dto);
        
        // 构建完整地址
        StringBuilder fullAddress = new StringBuilder();
        if (address.getProvince() != null) {
            fullAddress.append(address.getProvince());
        }
        if (address.getCity() != null) {
            fullAddress.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            fullAddress.append(address.getDistrict());
        }
        if (address.getDetailAddress() != null) {
            fullAddress.append(address.getDetailAddress());
        }
        dto.setFullAddress(fullAddress.toString());
        
        return dto;
    }
}

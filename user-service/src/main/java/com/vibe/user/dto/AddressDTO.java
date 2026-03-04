package com.vibe.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址DTO
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Data
public class AddressDTO {
    
    /**
     * 地址ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String detailAddress;
    
    /**
     * 完整地址
     */
    private String fullAddress;
    
    /**
     * 邮编
     */
    private String postalCode;
    
    /**
     * 是否默认地址：0-否，1-是
     */
    private Integer isDefault;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

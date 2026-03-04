package com.vibe.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.coupon.dto.CouponCreateDTO;
import com.vibe.coupon.dto.CouponDTO;
import com.vibe.coupon.entity.Coupon;
import com.vibe.coupon.enums.CouponDistributeType;
import com.vibe.coupon.enums.CouponType;
import com.vibe.coupon.mapper.CouponMapper;
import com.vibe.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class CouponServiceImpl implements CouponService {
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "COUPON_CREATE", resource = "COUPON", action = "CREATE")
    @MonitorPerformance(threshold = 2000, operation = "优惠券创建")
    public CouponDTO createCoupon(CouponCreateDTO createDTO) {
        LogUtils.businessLog("COUPON_CREATE", "创建优惠券开始", createDTO.getName());
        
        // 验证优惠券类型和金额
        CouponType couponType = CouponType.getByCode(createDTO.getType());
        if (couponType == null) {
            throw new BusinessException(400, "不支持的优惠券类型");
        }
        
        if (couponType == CouponType.FULL_REDUCTION) {
            if (createDTO.getDiscountAmount() == null || createDTO.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, "满减券必须设置优惠金额");
            }
        } else if (couponType == CouponType.DISCOUNT) {
            if (createDTO.getDiscountRate() == null || createDTO.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0 
                    || createDTO.getDiscountRate().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException(400, "折扣券折扣率必须在0-100之间");
            }
        }
        
        // 验证时间
        if (createDTO.getEndTime().isBefore(createDTO.getStartTime())) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }
        
        Coupon coupon = Coupon.builder()
                .name(createDTO.getName())
                .type(createDTO.getType())
                .discountAmount(createDTO.getDiscountAmount() != null ? 
                        MoneyUtils.createMoney(createDTO.getDiscountAmount()) : null)
                .discountRate(createDTO.getDiscountRate())
                .minAmount(createDTO.getMinAmount() != null ? 
                        MoneyUtils.createMoney(createDTO.getMinAmount()) : MoneyUtils.createMoney("0.00"))
                .totalCount(createDTO.getTotalCount())
                .usedCount(0)
                .receivedCount(0)
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .status(1)  // 默认启用
                .distributeType(createDTO.getDistributeType() != null ? 
                        createDTO.getDistributeType() : CouponDistributeType.MANUAL.getCode())
                .limitPerUser(createDTO.getLimitPerUser() != null ? createDTO.getLimitPerUser() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        couponMapper.insert(coupon);
        
        LogUtils.businessLog("COUPON_CREATE", "优惠券创建成功", coupon.getId(), coupon.getName());
        
        return convertToDTO(coupon);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "优惠券查询")
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "优惠券不存在");
        }
        return convertToDTO(coupon);
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "优惠券列表查询")
    public Page<CouponDTO> listCoupons(Integer page, Integer size, Integer status) {
        Page<Coupon> couponPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getIsDeleted, 0);
        
        if (status != null) {
            queryWrapper.eq(Coupon::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Coupon::getCreateTime);
        
        Page<Coupon> result = couponMapper.selectPage(couponPage, queryWrapper);
        
        // 转换为DTO
        Page<CouponDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<CouponDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "可用优惠券查询")
    public List<CouponDTO> listAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getStatus, 1)
                .eq(Coupon::getIsDeleted, 0)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .apply("received_count < total_count")  // 还有剩余数量
                .orderByDesc(Coupon::getCreateTime);
        
        List<Coupon> coupons = couponMapper.selectList(queryWrapper);
        
        return coupons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "COUPON_DISTRIBUTE", resource = "COUPON", action = "UPDATE")
    public int distributeCoupon(Long couponId, List<Long> userIds) {
        LogUtils.businessLog("COUPON_DISTRIBUTE", "发放优惠券开始", couponId, userIds.size());
        
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        // TODO: 调用UserCouponService批量发放
        // 这里简化处理，实际应该批量插入用户优惠券记录
        
        int successCount = userIds.size();
        couponMapper.increaseReceivedCount(couponId, successCount);
        
        LogUtils.businessLog("COUPON_DISTRIBUTE", "发放优惠券完成", couponId, successCount);
        return successCount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "COUPON_TOGGLE_STATUS", resource = "COUPON", action = "UPDATE")
    public boolean toggleCouponStatus(Long id, boolean enabled) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        coupon.setStatus(enabled ? 1 : 0);
        coupon.setUpdateTime(LocalDateTime.now());
        couponMapper.updateById(coupon);
        
        LogUtils.businessLog("COUPON_TOGGLE_STATUS", enabled ? "启用优惠券" : "禁用优惠券", id);
        return true;
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "优惠金额计算")
    public BigDecimal calculateDiscount(Long couponId, BigDecimal orderAmount) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getIsDeleted() == 1) {
            throw new BusinessException(404, "优惠券不存在");
        }
        
        // 检查最低消费金额
        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            return MoneyUtils.createMoney("0.00");
        }
        
        CouponType couponType = CouponType.getByCode(coupon.getType());
        BigDecimal discount = MoneyUtils.createMoney("0.00");
        
        switch (couponType) {
            case FULL_REDUCTION:
                // 满减券：直接返回优惠金额
                discount = coupon.getDiscountAmount();
                break;
            case DISCOUNT:
                // 折扣券：计算折扣金额
                BigDecimal discountRate = coupon.getDiscountRate().divide(new BigDecimal("100"));
                discount = MoneyUtils.multiply(orderAmount, discountRate);
                break;
            case FREE_SHIPPING:
                // 免邮券：这里简化处理，实际应该返回运费金额
                discount = MoneyUtils.createMoney("0.00");
                break;
        }
        
        // 确保优惠金额不超过订单金额
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }
    
    /**
     * 转换为DTO
     * 
     * @param coupon 优惠券实体
     * @return 优惠券DTO
     */
    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        BeanUtils.copyProperties(coupon, dto);
        
        CouponType type = CouponType.getByCode(coupon.getType());
        if (type != null) {
            dto.setTypeDesc(type.getDescription());
        }
        
        // 计算剩余数量
        dto.setRemainingCount(coupon.getTotalCount() - coupon.getReceivedCount());
        
        return dto;
    }
}

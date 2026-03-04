package com.vibe.product.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.product.dto.ProductSkuDTO;
import com.vibe.product.entity.ProductSku;
import com.vibe.product.mapper.ProductSkuMapper;
import com.vibe.product.service.ProductSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品SKU服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class ProductSkuServiceImpl implements ProductSkuService {
    
    @Autowired
    private ProductSkuMapper skuMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "SKU_CREATE", resource = "SKU", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "SKU创建")
    public ProductSkuDTO createSku(ProductSkuDTO skuDTO) {
        LogUtils.businessLog("SKU_CREATE", "创建SKU开始", skuDTO.getProductId());
        
        // 生成SKU编码
        String skuCode = skuDTO.getSkuCode();
        if (skuCode == null || skuCode.isEmpty()) {
            skuCode = "SKU" + IdUtil.getSnowflake(1, 1).nextIdStr();
        }
        
        // 检查SKU编码是否已存在
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSku::getSkuCode, skuCode)
                .eq(ProductSku::getIsDeleted, 0);
        ProductSku existSku = skuMapper.selectOne(queryWrapper);
        if (existSku != null) {
            throw new BusinessException(400, "SKU编码已存在");
        }
        
        // 转换规格为JSON
        String specsJson = null;
        if (skuDTO.getSpecs() != null && !skuDTO.getSpecs().isEmpty()) {
            try {
                specsJson = objectMapper.writeValueAsString(skuDTO.getSpecs());
            } catch (Exception e) {
                log.error("规格转换JSON失败", e);
                throw new BusinessException(500, "规格格式错误");
            }
        }
        
        ProductSku sku = ProductSku.builder()
                .productId(skuDTO.getProductId())
                .skuCode(skuCode)
                .skuName(skuDTO.getSkuName())
                .specs(specsJson)
                .price(MoneyUtils.createMoney(skuDTO.getPrice()))
                .stock(skuDTO.getStock())
                .image(skuDTO.getImage())
                .status(skuDTO.getStatus() != null ? skuDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        skuMapper.insert(sku);
        
        LogUtils.businessLog("SKU_CREATE", "SKU创建成功", sku.getId(), skuCode);
        
        return convertToDTO(sku);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "SKU_UPDATE", resource = "SKU", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "SKU更新")
    public ProductSkuDTO updateSku(ProductSkuDTO skuDTO) {
        LogUtils.businessLog("SKU_UPDATE", "更新SKU开始", skuDTO.getId());
        
        ProductSku sku = skuMapper.selectById(skuDTO.getId());
        if (sku == null || sku.getIsDeleted() == 1) {
            throw new BusinessException(404, "SKU不存在");
        }
        
        // 更新字段
        if (skuDTO.getSkuName() != null) {
            sku.setSkuName(skuDTO.getSkuName());
        }
        if (skuDTO.getSpecs() != null) {
            try {
                sku.setSpecs(objectMapper.writeValueAsString(skuDTO.getSpecs()));
            } catch (Exception e) {
                log.error("规格转换JSON失败", e);
                throw new BusinessException(500, "规格格式错误");
            }
        }
        if (skuDTO.getPrice() != null) {
            sku.setPrice(MoneyUtils.createMoney(skuDTO.getPrice()));
        }
        if (skuDTO.getStock() != null) {
            sku.setStock(skuDTO.getStock());
        }
        if (skuDTO.getImage() != null) {
            sku.setImage(skuDTO.getImage());
        }
        if (skuDTO.getStatus() != null) {
            sku.setStatus(skuDTO.getStatus());
        }
        sku.setUpdateTime(LocalDateTime.now());
        
        skuMapper.updateById(sku);
        
        LogUtils.businessLog("SKU_UPDATE", "SKU更新成功", sku.getId());
        
        return convertToDTO(sku);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "SKU查询")
    public ProductSkuDTO getSkuById(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null || sku.getIsDeleted() == 1) {
            throw new BusinessException(404, "SKU不存在");
        }
        return convertToDTO(sku);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "SKU列表查询")
    public List<ProductSkuDTO> getSkusByProductId(Long productId) {
        LambdaQueryWrapper<ProductSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getIsDeleted, 0)
                .orderByAsc(ProductSku::getId);
        
        List<ProductSku> skus = skuMapper.selectList(queryWrapper);
        
        return skus.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "SKU_DELETE", resource = "SKU", action = "DELETE")
    public boolean deleteSku(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null || sku.getIsDeleted() == 1) {
            throw new BusinessException(404, "SKU不存在");
        }
        
        sku.setIsDeleted(1);
        sku.setUpdateTime(LocalDateTime.now());
        skuMapper.updateById(sku);
        
        LogUtils.businessLog("SKU_DELETE", "SKU删除成功", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "SKU_DEDUCT_STOCK", resource = "SKU", action = "UPDATE")
    public boolean deductStock(Long skuId, Integer quantity) {
        int result = skuMapper.deductStock(skuId, quantity);
        if (result <= 0) {
            throw new BusinessException(400, "SKU库存不足");
        }
        LogUtils.businessLog("SKU_DEDUCT_STOCK", "SKU库存扣减成功", skuId, quantity);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "SKU_ROLLBACK_STOCK", resource = "SKU", action = "UPDATE")
    public boolean rollbackStock(Long skuId, Integer quantity) {
        int result = skuMapper.rollbackStock(skuId, quantity);
        if (result <= 0) {
            throw new BusinessException(400, "SKU库存回滚失败");
        }
        LogUtils.businessLog("SKU_ROLLBACK_STOCK", "SKU库存回滚成功", skuId, quantity);
        return true;
    }
    
    /**
     * 转换为DTO
     * 
     * @param sku SKU实体
     * @return SKU DTO
     */
    private ProductSkuDTO convertToDTO(ProductSku sku) {
        ProductSkuDTO dto = new ProductSkuDTO();
        BeanUtils.copyProperties(sku, dto);
        
        // 转换规格JSON为Map
        if (sku.getSpecs() != null && !sku.getSpecs().isEmpty()) {
            try {
                Map<String, String> specs = objectMapper.readValue(
                        sku.getSpecs(), 
                        new TypeReference<Map<String, String>>() {});
                dto.setSpecs(specs);
            } catch (Exception e) {
                log.error("规格JSON解析失败", e);
            }
        }
        
        return dto;
    }
}

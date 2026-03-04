package com.vibe.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.inventory.dto.InventoryDeductDTO;
import com.vibe.inventory.entity.Inventory;
import com.vibe.inventory.mapper.InventoryMapper;
import com.vibe.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    
    @Autowired
    private InventoryMapper inventoryMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @MonitorPerformance(threshold = 1000, operation = "库存扣减")
    public boolean deductStock(InventoryDeductDTO deductDTO) {
        log.info("扣减库存，商品ID: {}, 数量: {}", deductDTO.getProductId(), deductDTO.getQuantity());
        
        // 查询库存
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Inventory::getProductId, deductDTO.getProductId())
                .eq(Inventory::getIsDeleted, 0);
        Inventory inventory = inventoryMapper.selectOne(queryWrapper);
        
        if (inventory == null) {
            throw new BusinessException(404, "商品库存不存在");
        }
        
        // 检查库存是否充足
        if (inventory.getStock() < deductDTO.getQuantity()) {
            throw new BusinessException(400, "库存不足");
        }
        
        // 扣减库存
        int result = inventoryMapper.deductStock(deductDTO.getProductId(), deductDTO.getQuantity());
        if (result <= 0) {
            throw new BusinessException(400, "库存扣减失败");
        }
        
        log.info("库存扣减成功，商品ID: {}, 扣减数量: {}", deductDTO.getProductId(), deductDTO.getQuantity());
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(Long productId, Integer quantity) {
        log.info("回滚库存，商品ID: {}, 数量: {}", productId, quantity);
        
        int result = inventoryMapper.rollbackStock(productId, quantity);
        if (result <= 0) {
            log.warn("库存回滚失败，商品ID: {}, 数量: {}", productId, quantity);
            return false;
        }
        
        log.info("库存回滚成功，商品ID: {}, 回滚数量: {}", productId, quantity);
        return true;
    }
    
    @Override
    public Integer getStock(Long productId) {
        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Inventory::getProductId, productId)
                .eq(Inventory::getIsDeleted, 0);
        Inventory inventory = inventoryMapper.selectOne(queryWrapper);
        
        if (inventory == null) {
            return 0;
        }
        
        return inventory.getStock();
    }
}

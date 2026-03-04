package com.vibe.inventory.controller;

import com.vibe.common.core.result.Result;
import com.vibe.inventory.dto.InventoryDeductDTO;
import com.vibe.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * 扣减库存
     * 
     * @param deductDTO 扣减DTO
     * @return 响应结果
     */
    @PostMapping("/deduct")
    public Result<Boolean> deductStock(@Validated @RequestBody InventoryDeductDTO deductDTO) {
        boolean result = inventoryService.deductStock(deductDTO);
        return Result.success("库存扣减成功", result);
    }
    
    /**
     * 回滚库存
     * 
     * @param productId 商品ID
     * @param quantity 回滚数量
     * @return 响应结果
     */
    @PostMapping("/rollback")
    public Result<Boolean> rollbackStock(@RequestParam Long productId, 
                                         @RequestParam Integer quantity) {
        boolean result = inventoryService.rollbackStock(productId, quantity);
        return Result.success("库存回滚成功", result);
    }
    
    /**
     * 查询库存
     * 
     * @param productId 商品ID
     * @return 响应结果
     */
    @GetMapping("/{productId}")
    public Result<Integer> getStock(@PathVariable Long productId) {
        Integer stock = inventoryService.getStock(productId);
        return Result.success(stock);
    }
}

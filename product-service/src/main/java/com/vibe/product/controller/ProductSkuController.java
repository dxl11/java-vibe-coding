package com.vibe.product.controller;

import com.vibe.common.core.result.Result;
import com.vibe.product.dto.ProductSkuDTO;
import com.vibe.product.service.ProductSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/product/sku")
public class ProductSkuController {
    
    @Autowired
    private ProductSkuService skuService;
    
    /**
     * 创建SKU
     * 
     * @param skuDTO SKU DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    public Result<ProductSkuDTO> createSku(@Validated @RequestBody ProductSkuDTO skuDTO) {
        ProductSkuDTO sku = skuService.createSku(skuDTO);
        return Result.success("SKU创建成功", sku);
    }
    
    /**
     * 更新SKU
     * 
     * @param skuDTO SKU DTO
     * @return 响应结果
     */
    @PutMapping("/update")
    public Result<ProductSkuDTO> updateSku(@Validated @RequestBody ProductSkuDTO skuDTO) {
        ProductSkuDTO sku = skuService.updateSku(skuDTO);
        return Result.success("SKU更新成功", sku);
    }
    
    /**
     * 根据ID查询SKU
     * 
     * @param id SKU ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result<ProductSkuDTO> getSkuById(@PathVariable Long id) {
        ProductSkuDTO sku = skuService.getSkuById(id);
        return Result.success(sku);
    }
    
    /**
     * 根据商品ID查询SKU列表
     * 
     * @param productId 商品ID
     * @return 响应结果
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductSkuDTO>> getSkusByProductId(@PathVariable Long productId) {
        List<ProductSkuDTO> skus = skuService.getSkusByProductId(productId);
        return Result.success(skus);
    }
    
    /**
     * 删除SKU
     * 
     * @param id SKU ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSku(@PathVariable Long id) {
        boolean result = skuService.deleteSku(id);
        return Result.success("SKU删除成功", result);
    }
}

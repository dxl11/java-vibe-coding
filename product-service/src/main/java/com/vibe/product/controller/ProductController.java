package com.vibe.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.common.core.result.Result;
import com.vibe.product.dto.ProductCreateDTO;
import com.vibe.product.dto.ProductDTO;
import com.vibe.product.dto.ProductUpdateDTO;
import com.vibe.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * 创建商品
     * 
     * @param createDTO 创建DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    public Result<ProductDTO> createProduct(@Validated @RequestBody ProductCreateDTO createDTO) {
        ProductDTO product = productService.createProduct(createDTO);
        return Result.success("商品创建成功", product);
    }
    
    /**
     * 更新商品
     * 
     * @param updateDTO 更新DTO
     * @return 响应结果
     */
    @PutMapping("/update")
    public Result<ProductDTO> updateProduct(@Validated @RequestBody ProductUpdateDTO updateDTO) {
        ProductDTO product = productService.updateProduct(updateDTO);
        return Result.success("商品更新成功", product);
    }
    
    /**
     * 根据ID查询商品
     * 
     * @param id 商品ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return Result.success(product);
    }
    
    /**
     * 分页查询商品
     * 
     * @param page 页码
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 响应结果
     */
    @GetMapping("/list")
    public Result<Page<ProductDTO>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<ProductDTO> productPage = productService.listProducts(page, size, categoryId, status, keyword);
        return Result.success(productPage);
    }
    
    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        boolean result = productService.deleteProduct(id);
        return Result.success("商品删除成功", result);
    }
    
    /**
     * 上架商品
     * 
     * @param id 商品ID
     * @return 响应结果
     */
    @PostMapping("/{id}/online")
    public Result<Boolean> onlineProduct(@PathVariable Long id) {
        boolean result = productService.onlineProduct(id);
        return Result.success("商品上架成功", result);
    }
    
    /**
     * 下架商品
     * 
     * @param id 商品ID
     * @return 响应结果
     */
    @PostMapping("/{id}/offline")
    public Result<Boolean> offlineProduct(@PathVariable Long id) {
        boolean result = productService.offlineProduct(id);
        return Result.success("商品下架成功", result);
    }
    
    /**
     * 审核商品
     * 
     * @param id 商品ID
     * @param approved 是否通过
     * @return 响应结果
     */
    @PostMapping("/{id}/audit")
    public Result<Boolean> auditProduct(@PathVariable Long id, @RequestParam boolean approved) {
        boolean result = productService.auditProduct(id, approved);
        return Result.success(approved ? "商品审核通过" : "商品审核驳回", result);
    }
    
    /**
     * 批量上架
     * 
     * @param ids 商品ID列表
     * @return 响应结果
     */
    @PostMapping("/batch/online")
    public Result<Integer> batchOnline(@RequestBody List<Long> ids) {
        int count = productService.batchOnline(ids);
        return Result.success("批量上架完成", count);
    }
    
    /**
     * 批量下架
     * 
     * @param ids 商品ID列表
     * @return 响应结果
     */
    @PostMapping("/batch/offline")
    public Result<Integer> batchOffline(@RequestBody List<Long> ids) {
        int count = productService.batchOffline(ids);
        return Result.success("批量下架完成", count);
    }
    
    /**
     * 批量删除
     * 
     * @param ids 商品ID列表
     * @return 响应结果
     */
    @PostMapping("/batch/delete")
    public Result<Integer> batchDelete(@RequestBody List<Long> ids) {
        int count = productService.batchDelete(ids);
        return Result.success("批量删除完成", count);
    }
}

package com.vibe.product.controller;

import com.vibe.common.core.result.Result;
import com.vibe.product.dto.ProductImageDTO;
import com.vibe.product.service.ProductImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品图片控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/product/image")
public class ProductImageController {
    
    @Autowired
    private ProductImageService imageService;
    
    /**
     * 添加商品图片
     * 
     * @param imageDTO 图片DTO
     * @return 响应结果
     */
    @PostMapping("/add")
    public Result<ProductImageDTO> addImage(@Validated @RequestBody ProductImageDTO imageDTO) {
        ProductImageDTO image = imageService.addImage(imageDTO);
        return Result.success("图片添加成功", image);
    }
    
    /**
     * 删除图片
     * 
     * @param id 图片ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteImage(@PathVariable Long id) {
        boolean result = imageService.deleteImage(id);
        return Result.success("图片删除成功", result);
    }
    
    /**
     * 设置主图
     * 
     * @param id 图片ID
     * @return 响应结果
     */
    @PostMapping("/{id}/set-main")
    public Result<Boolean> setMainImage(@PathVariable Long id) {
        boolean result = imageService.setMainImage(id);
        return Result.success("设置主图成功", result);
    }
    
    /**
     * 根据商品ID查询图片列表
     * 
     * @param productId 商品ID
     * @param imageType 图片类型（可选）
     * @return 响应结果
     */
    @GetMapping("/product/{productId}")
    public Result<List<ProductImageDTO>> getImagesByProductId(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer imageType) {
        List<ProductImageDTO> images = imageService.getImagesByProductId(productId, imageType);
        return Result.success(images);
    }
    
    /**
     * 批量添加图片
     * 
     * @param productId 商品ID
     * @param imageUrls 图片URL列表
     * @param imageType 图片类型
     * @return 响应结果
     */
    @PostMapping("/batch/add")
    public Result<Integer> batchAddImages(
            @RequestParam Long productId,
            @RequestBody List<String> imageUrls,
            @RequestParam(required = false) Integer imageType) {
        int count = imageService.batchAddImages(productId, imageUrls, imageType);
        return Result.success("批量添加图片完成", count);
    }
}

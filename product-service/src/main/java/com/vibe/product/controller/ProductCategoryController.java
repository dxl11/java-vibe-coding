package com.vibe.product.controller;

import com.vibe.common.core.result.Result;
import com.vibe.product.dto.ProductCategoryDTO;
import com.vibe.product.service.ProductCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@RestController
@RequestMapping("/product/category")
public class ProductCategoryController {
    
    @Autowired
    private ProductCategoryService categoryService;
    
    /**
     * 创建分类
     * 
     * @param categoryDTO 分类DTO
     * @return 响应结果
     */
    @PostMapping("/create")
    public Result<ProductCategoryDTO> createCategory(@Validated @RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO category = categoryService.createCategory(categoryDTO);
        return Result.success("分类创建成功", category);
    }
    
    /**
     * 更新分类
     * 
     * @param categoryDTO 分类DTO
     * @return 响应结果
     */
    @PutMapping("/update")
    public Result<ProductCategoryDTO> updateCategory(@Validated @RequestBody ProductCategoryDTO categoryDTO) {
        ProductCategoryDTO category = categoryService.updateCategory(categoryDTO);
        return Result.success("分类更新成功", category);
    }
    
    /**
     * 根据ID查询分类
     * 
     * @param id 分类ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Result<ProductCategoryDTO> getCategoryById(@PathVariable Long id) {
        ProductCategoryDTO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
    
    /**
     * 查询分类树
     * 
     * @return 响应结果
     */
    @GetMapping("/tree")
    public Result<List<ProductCategoryDTO>> getCategoryTree() {
        List<ProductCategoryDTO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }
    
    /**
     * 根据父分类ID查询子分类
     * 
     * @param parentId 父分类ID
     * @return 响应结果
     */
    @GetMapping("/parent/{parentId}")
    public Result<List<ProductCategoryDTO>> getCategoriesByParentId(@PathVariable Long parentId) {
        List<ProductCategoryDTO> categories = categoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }
    
    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        boolean result = categoryService.deleteCategory(id);
        return Result.success("分类删除成功", result);
    }
}

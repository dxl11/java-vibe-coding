package com.vibe.product.service;

import com.vibe.product.dto.ProductCategoryDTO;

import java.util.List;

/**
 * 商品分类服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface ProductCategoryService {
    
    /**
     * 创建分类
     * 
     * @param categoryDTO 分类DTO
     * @return 分类DTO
     */
    ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO);
    
    /**
     * 更新分类
     * 
     * @param categoryDTO 分类DTO
     * @return 分类DTO
     */
    ProductCategoryDTO updateCategory(ProductCategoryDTO categoryDTO);
    
    /**
     * 根据ID查询分类
     * 
     * @param id 分类ID
     * @return 分类DTO
     */
    ProductCategoryDTO getCategoryById(Long id);
    
    /**
     * 查询所有分类（树形结构）
     * 
     * @return 分类树
     */
    List<ProductCategoryDTO> getCategoryTree();
    
    /**
     * 根据父分类ID查询子分类
     * 
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategoryDTO> getCategoriesByParentId(Long parentId);
    
    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);
}

package com.vibe.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.product.dto.ProductCategoryDTO;
import com.vibe.product.entity.ProductCategory;
import com.vibe.product.mapper.ProductCategoryMapper;
import com.vibe.product.service.ProductCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    
    @Autowired
    private ProductCategoryMapper categoryMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CATEGORY_CREATE", resource = "CATEGORY", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "分类创建")
    public ProductCategoryDTO createCategory(ProductCategoryDTO categoryDTO) {
        LogUtils.businessLog("CATEGORY_CREATE", "创建分类开始", categoryDTO.getName());
        
        // 检查父分类是否存在
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0) {
            ProductCategory parent = categoryMapper.selectById(categoryDTO.getParentId());
            if (parent == null || parent.getIsDeleted() == 1) {
                throw new BusinessException(404, "父分类不存在");
            }
        }
        
        ProductCategory category = ProductCategory.builder()
                .name(categoryDTO.getName())
                .parentId(categoryDTO.getParentId() != null ? categoryDTO.getParentId() : 0L)
                .sortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0)
                .icon(categoryDTO.getIcon())
                .description(categoryDTO.getDescription())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        categoryMapper.insert(category);
        
        LogUtils.businessLog("CATEGORY_CREATE", "分类创建成功", category.getId(), category.getName());
        
        return convertToDTO(category);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CATEGORY_UPDATE", resource = "CATEGORY", action = "UPDATE")
    @MonitorPerformance(threshold = 1000, operation = "分类更新")
    public ProductCategoryDTO updateCategory(ProductCategoryDTO categoryDTO) {
        LogUtils.businessLog("CATEGORY_UPDATE", "更新分类开始", categoryDTO.getId());
        
        ProductCategory category = categoryMapper.selectById(categoryDTO.getId());
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(404, "分类不存在");
        }
        
        // 更新字段
        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }
        if (categoryDTO.getParentId() != null) {
            // 不能将分类设置为自己的子分类
            if (categoryDTO.getParentId().equals(category.getId())) {
                throw new BusinessException(400, "不能将分类设置为自己的父分类");
            }
            category.setParentId(categoryDTO.getParentId());
        }
        if (categoryDTO.getSortOrder() != null) {
            category.setSortOrder(categoryDTO.getSortOrder());
        }
        if (categoryDTO.getIcon() != null) {
            category.setIcon(categoryDTO.getIcon());
        }
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }
        category.setUpdateTime(LocalDateTime.now());
        
        categoryMapper.updateById(category);
        
        LogUtils.businessLog("CATEGORY_UPDATE", "分类更新成功", category.getId());
        
        return convertToDTO(category);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "分类查询")
    public ProductCategoryDTO getCategoryById(Long id) {
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(404, "分类不存在");
        }
        return convertToDTO(category);
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "分类树查询")
    public List<ProductCategoryDTO> getCategoryTree() {
        // 查询所有未删除的分类
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getIsDeleted, 0)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId);
        
        List<ProductCategory> categories = categoryMapper.selectList(queryWrapper);
        
        // 转换为DTO
        List<ProductCategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(categoryDTOs, 0L);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "子分类查询")
    public List<ProductCategoryDTO> getCategoriesByParentId(Long parentId) {
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getParentId, parentId)
                .eq(ProductCategory::getIsDeleted, 0)
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getId);
        
        List<ProductCategory> categories = categoryMapper.selectList(queryWrapper);
        
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "CATEGORY_DELETE", resource = "CATEGORY", action = "DELETE")
    public boolean deleteCategory(Long id) {
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(404, "分类不存在");
        }
        
        // 检查是否有子分类
        List<ProductCategoryDTO> children = getCategoriesByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(400, "存在子分类，无法删除");
        }
        
        category.setIsDeleted(1);
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        
        LogUtils.businessLog("CATEGORY_DELETE", "分类删除成功", id);
        return true;
    }
    
    /**
     * 构建树形结构
     * 
     * @param categories 分类列表
     * @param parentId 父分类ID
     * @return 树形分类列表
     */
    private List<ProductCategoryDTO> buildTree(List<ProductCategoryDTO> categories, Long parentId) {
        List<ProductCategoryDTO> tree = new ArrayList<>();
        
        for (ProductCategoryDTO category : categories) {
            if (category.getParentId() != null && category.getParentId().equals(parentId)) {
                List<ProductCategoryDTO> children = buildTree(categories, category.getId());
                category.setChildren(children);
                tree.add(category);
            }
        }
        
        return tree;
    }
    
    /**
     * 转换为DTO
     * 
     * @param category 分类实体
     * @return 分类DTO
     */
    private ProductCategoryDTO convertToDTO(ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }
}

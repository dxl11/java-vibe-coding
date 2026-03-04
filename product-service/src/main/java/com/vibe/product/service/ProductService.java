package com.vibe.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.product.dto.ProductCreateDTO;
import com.vibe.product.dto.ProductDTO;
import com.vibe.product.dto.ProductUpdateDTO;

import java.util.List;

/**
 * 商品服务接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
public interface ProductService {
    
    /**
     * 创建商品
     * 
     * @param createDTO 创建DTO
     * @return 商品DTO
     */
    ProductDTO createProduct(ProductCreateDTO createDTO);
    
    /**
     * 更新商品
     * 
     * @param updateDTO 更新DTO
     * @return 商品DTO
     */
    ProductDTO updateProduct(ProductUpdateDTO updateDTO);
    
    /**
     * 根据ID查询商品
     * 
     * @param id 商品ID
     * @return 商品DTO
     */
    ProductDTO getProductById(Long id);
    
    /**
     * 分页查询商品
     * 
     * @param page 页码
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 分页结果
     */
    Page<ProductDTO> listProducts(Integer page, Integer size, Long categoryId, Integer status, String keyword);
    
    /**
     * 删除商品（逻辑删除）
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);
    
    /**
     * 上架商品
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    boolean onlineProduct(Long id);
    
    /**
     * 下架商品
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    boolean offlineProduct(Long id);
    
    /**
     * 审核商品
     * 
     * @param id 商品ID
     * @param approved 是否通过
     * @return 是否成功
     */
    boolean auditProduct(Long id, boolean approved);
    
    /**
     * 批量上架
     * 
     * @param ids 商品ID列表
     * @return 成功数量
     */
    int batchOnline(List<Long> ids);
    
    /**
     * 批量下架
     * 
     * @param ids 商品ID列表
     * @return 成功数量
     */
    int batchOffline(List<Long> ids);
    
    /**
     * 批量删除
     * 
     * @param ids 商品ID列表
     * @return 成功数量
     */
    int batchDelete(List<Long> ids);
}

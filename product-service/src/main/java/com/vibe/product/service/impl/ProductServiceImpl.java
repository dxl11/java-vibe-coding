package com.vibe.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.common.core.util.MoneyUtils;
import com.vibe.product.dto.ProductCreateDTO;
import com.vibe.product.dto.ProductDTO;
import com.vibe.product.dto.ProductUpdateDTO;
import com.vibe.product.entity.Product;
import com.vibe.product.enums.ProductStatus;
import com.vibe.product.mapper.ProductMapper;
import com.vibe.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_CREATE", resource = "PRODUCT", action = "CREATE")
    @MonitorPerformance(threshold = 2000, operation = "商品创建")
    public ProductDTO createProduct(ProductCreateDTO createDTO) {
        LogUtils.businessLog("PRODUCT_CREATE", "创建商品开始", createDTO.getName());
        
        // 创建商品实体
        Product product = Product.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .price(MoneyUtils.createMoney(createDTO.getPrice()))
                .stock(createDTO.getStock())
                .categoryId(createDTO.getCategoryId())
                .status(ProductStatus.PENDING_AUDIT.getCode())  // 默认待审核
                .detail(createDTO.getDetail())
                .seoKeywords(createDTO.getSeoKeywords())
                .seoDescription(createDTO.getSeoDescription())
                .sales(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        productMapper.insert(product);
        
        LogUtils.businessLog("PRODUCT_CREATE", "商品创建成功", product.getId(), product.getName());
        
        return convertToDTO(product);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_UPDATE", resource = "PRODUCT", action = "UPDATE")
    @MonitorPerformance(threshold = 2000, operation = "商品更新")
    public ProductDTO updateProduct(ProductUpdateDTO updateDTO) {
        LogUtils.businessLog("PRODUCT_UPDATE", "更新商品开始", updateDTO.getId());
        
        Product product = productMapper.selectById(updateDTO.getId());
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 更新字段
        if (StringUtils.hasText(updateDTO.getName())) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(MoneyUtils.createMoney(updateDTO.getPrice()));
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
        if (updateDTO.getCategoryId() != null) {
            product.setCategoryId(updateDTO.getCategoryId());
        }
        if (updateDTO.getDetail() != null) {
            product.setDetail(updateDTO.getDetail());
        }
        if (updateDTO.getSeoKeywords() != null) {
            product.setSeoKeywords(updateDTO.getSeoKeywords());
        }
        if (updateDTO.getSeoDescription() != null) {
            product.setSeoDescription(updateDTO.getSeoDescription());
        }
        product.setUpdateTime(LocalDateTime.now());
        
        productMapper.updateById(product);
        
        LogUtils.businessLog("PRODUCT_UPDATE", "商品更新成功", product.getId());
        
        return convertToDTO(product);
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "商品查询")
    public ProductDTO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        return convertToDTO(product);
    }
    
    @Override
    @MonitorPerformance(threshold = 1000, operation = "商品列表查询")
    public Page<ProductDTO> listProducts(Integer page, Integer size, Long categoryId, Integer status, String keyword) {
        Page<Product> productPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getIsDeleted, 0);
        
        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }
        
        queryWrapper.orderByDesc(Product::getCreateTime);
        
        Page<Product> result = productMapper.selectPage(productPage, queryWrapper);
        
        // 转换为DTO
        Page<ProductDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ProductDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_DELETE", resource = "PRODUCT", action = "DELETE")
    public boolean deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        
        product.setIsDeleted(1);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        
        LogUtils.businessLog("PRODUCT_DELETE", "商品删除成功", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_ONLINE", resource = "PRODUCT", action = "UPDATE")
    public boolean onlineProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 只有待审核或已驳回状态的商品才能上架
        if (product.getStatus() != ProductStatus.PENDING_AUDIT.getCode() 
                && product.getStatus() != ProductStatus.REJECTED.getCode()) {
            throw new BusinessException(400, "商品状态不允许上架");
        }
        
        productMapper.updateStatus(id, ProductStatus.ONLINE.getCode());
        
        LogUtils.businessLog("PRODUCT_ONLINE", "商品上架成功", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_OFFLINE", resource = "PRODUCT", action = "UPDATE")
    public boolean offlineProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 只有上架状态的商品才能下架
        if (product.getStatus() != ProductStatus.ONLINE.getCode()) {
            throw new BusinessException(400, "商品状态不允许下架");
        }
        
        productMapper.updateStatus(id, ProductStatus.OFFLINE.getCode());
        
        LogUtils.businessLog("PRODUCT_OFFLINE", "商品下架成功", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_AUDIT", resource = "PRODUCT", action = "UPDATE")
    public boolean auditProduct(Long id, boolean approved) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 只有待审核状态的商品才能审核
        if (product.getStatus() != ProductStatus.PENDING_AUDIT.getCode()) {
            throw new BusinessException(400, "商品状态不允许审核");
        }
        
        Integer newStatus = approved ? ProductStatus.ONLINE.getCode() : ProductStatus.REJECTED.getCode();
        productMapper.updateStatus(id, newStatus);
        
        LogUtils.businessLog("PRODUCT_AUDIT", approved ? "商品审核通过" : "商品审核驳回", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_BATCH_ONLINE", resource = "PRODUCT", action = "UPDATE")
    public int batchOnline(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            try {
                if (onlineProduct(id)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量上架失败，商品ID: {}", id, e);
            }
        }
        LogUtils.businessLog("PRODUCT_BATCH_ONLINE", "批量上架完成", successCount, ids.size());
        return successCount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_BATCH_OFFLINE", resource = "PRODUCT", action = "UPDATE")
    public int batchOffline(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            try {
                if (offlineProduct(id)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量下架失败，商品ID: {}", id, e);
            }
        }
        LogUtils.businessLog("PRODUCT_BATCH_OFFLINE", "批量下架完成", successCount, ids.size());
        return successCount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_BATCH_DELETE", resource = "PRODUCT", action = "DELETE")
    public int batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            try {
                if (deleteProduct(id)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量删除失败，商品ID: {}", id, e);
            }
        }
        LogUtils.businessLog("PRODUCT_BATCH_DELETE", "批量删除完成", successCount, ids.size());
        return successCount;
    }
    
    /**
     * 转换为DTO
     * 
     * @param product 商品实体
     * @return 商品DTO
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        
        ProductStatus status = ProductStatus.getByCode(product.getStatus());
        if (status != null) {
            dto.setStatusDesc(status.getDescription());
        }
        
        // TODO: 查询分类名称
        // dto.setCategoryName(categoryService.getCategoryName(product.getCategoryId()));
        
        return dto;
    }
}

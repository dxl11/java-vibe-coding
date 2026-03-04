package com.vibe.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vibe.common.core.exception.BusinessException;
import com.vibe.common.core.log.LogUtils;
import com.vibe.common.core.log.annotation.LogOperation;
import com.vibe.common.core.monitor.annotation.MonitorPerformance;
import com.vibe.product.dto.ProductImageDTO;
import com.vibe.product.entity.ProductImage;
import com.vibe.product.mapper.ProductImageMapper;
import com.vibe.product.service.ProductImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品图片服务实现类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Service
public class ProductImageServiceImpl implements ProductImageService {
    
    @Autowired
    private ProductImageMapper imageMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_IMAGE_ADD", resource = "PRODUCT_IMAGE", action = "CREATE")
    @MonitorPerformance(threshold = 1000, operation = "商品图片添加")
    public ProductImageDTO addImage(ProductImageDTO imageDTO) {
        LogUtils.businessLog("PRODUCT_IMAGE_ADD", "添加商品图片开始", imageDTO.getProductId());
        
        // 如果设置为主图，先取消其他主图
        if (imageDTO.getIsMain() != null && imageDTO.getIsMain() == 1) {
            imageMapper.cancelMainImage(imageDTO.getProductId());
        }
        
        ProductImage image = ProductImage.builder()
                .productId(imageDTO.getProductId())
                .imageUrl(imageDTO.getImageUrl())
                .imageType(imageDTO.getImageType() != null ? imageDTO.getImageType() : 1)
                .isMain(imageDTO.getIsMain() != null ? imageDTO.getIsMain() : 0)
                .sortOrder(imageDTO.getSortOrder() != null ? imageDTO.getSortOrder() : 0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        imageMapper.insert(image);
        
        LogUtils.businessLog("PRODUCT_IMAGE_ADD", "商品图片添加成功", image.getId());
        
        return convertToDTO(image);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_IMAGE_DELETE", resource = "PRODUCT_IMAGE", action = "DELETE")
    public boolean deleteImage(Long id) {
        ProductImage image = imageMapper.selectById(id);
        if (image == null || image.getIsDeleted() == 1) {
            throw new BusinessException(404, "图片不存在");
        }
        
        image.setIsDeleted(1);
        image.setUpdateTime(LocalDateTime.now());
        imageMapper.updateById(image);
        
        LogUtils.businessLog("PRODUCT_IMAGE_DELETE", "商品图片删除成功", id);
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 20)
    @LogOperation(operation = "PRODUCT_IMAGE_SET_MAIN", resource = "PRODUCT_IMAGE", action = "UPDATE")
    public boolean setMainImage(Long id) {
        ProductImage image = imageMapper.selectById(id);
        if (image == null || image.getIsDeleted() == 1) {
            throw new BusinessException(404, "图片不存在");
        }
        
        // 取消其他主图
        imageMapper.cancelMainImage(image.getProductId());
        
        // 设置当前图片为主图
        imageMapper.setMainImage(id);
        
        LogUtils.businessLog("PRODUCT_IMAGE_SET_MAIN", "设置主图成功", id);
        return true;
    }
    
    @Override
    @MonitorPerformance(threshold = 500, operation = "商品图片查询")
    public List<ProductImageDTO> getImagesByProductId(Long productId, Integer imageType) {
        LambdaQueryWrapper<ProductImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductImage::getProductId, productId)
                .eq(ProductImage::getIsDeleted, 0);
        
        if (imageType != null) {
            queryWrapper.eq(ProductImage::getImageType, imageType);
        }
        
        queryWrapper.orderByAsc(ProductImage::getIsMain)
                .orderByAsc(ProductImage::getSortOrder)
                .orderByAsc(ProductImage::getId);
        
        List<ProductImage> images = imageMapper.selectList(queryWrapper);
        
        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    @LogOperation(operation = "PRODUCT_IMAGE_BATCH_ADD", resource = "PRODUCT_IMAGE", action = "CREATE")
    public int batchAddImages(Long productId, List<String> imageUrls, Integer imageType) {
        int successCount = 0;
        int sortOrder = 0;
        
        for (String imageUrl : imageUrls) {
            try {
                ProductImageDTO imageDTO = new ProductImageDTO();
                imageDTO.setProductId(productId);
                imageDTO.setImageUrl(imageUrl);
                imageDTO.setImageType(imageType != null ? imageType : 1);
                imageDTO.setIsMain(successCount == 0 ? 1 : 0);  // 第一张设为主图
                imageDTO.setSortOrder(sortOrder++);
                
                addImage(imageDTO);
                successCount++;
            } catch (Exception e) {
                log.error("批量添加图片失败，ProductId: {}, ImageUrl: {}", productId, imageUrl, e);
            }
        }
        
        LogUtils.businessLog("PRODUCT_IMAGE_BATCH_ADD", "批量添加图片完成", successCount, imageUrls.size());
        return successCount;
    }
    
    /**
     * 转换为DTO
     * 
     * @param image 图片实体
     * @return 图片DTO
     */
    private ProductImageDTO convertToDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        BeanUtils.copyProperties(image, dto);
        return dto;
    }
}

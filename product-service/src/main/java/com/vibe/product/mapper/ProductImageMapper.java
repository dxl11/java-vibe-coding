package com.vibe.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.product.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品图片Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {
    
    /**
     * 取消其他主图
     * 
     * @param productId 商品ID
     * @return 更新行数
     */
    @Update("UPDATE product_image SET is_main = 0, update_time = NOW() " +
            "WHERE product_id = #{productId} AND is_main = 1 AND is_deleted = 0")
    int cancelMainImage(@Param("productId") Long productId);
    
    /**
     * 设置主图
     * 
     * @param id 图片ID
     * @return 更新行数
     */
    @Update("UPDATE product_image SET is_main = 1, update_time = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int setMainImage(@Param("id") Long id);
}

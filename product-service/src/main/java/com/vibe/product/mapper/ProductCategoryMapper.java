package com.vibe.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}

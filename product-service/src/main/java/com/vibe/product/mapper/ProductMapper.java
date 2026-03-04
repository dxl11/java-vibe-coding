package com.vibe.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 更新商品状态
     * 
     * @param id 商品ID
     * @param status 状态
     * @return 更新行数
     */
    @Update("UPDATE product SET status = #{status}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 增加销量
     * 
     * @param id 商品ID
     * @param quantity 数量
     * @return 更新行数
     */
    @Update("UPDATE product SET sales = sales + #{quantity}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int increaseSales(@Param("id") Long id, @Param("quantity") Integer quantity);
}

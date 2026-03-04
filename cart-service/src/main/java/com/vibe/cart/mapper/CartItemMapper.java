package com.vibe.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vibe.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 购物车项Mapper接口
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    
    /**
     * 根据用户ID查询购物车项
     * 
     * @param userId 用户ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY update_time DESC")
    List<CartItem> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据临时用户ID查询购物车项
     * 
     * @param tempUserId 临时用户ID
     * @return 购物车项列表
     */
    @Select("SELECT * FROM cart WHERE temp_user_id = #{tempUserId} AND is_deleted = 0 ORDER BY update_time DESC")
    List<CartItem> selectByTempUserId(@Param("tempUserId") String tempUserId);
    
    /**
     * 查询用户购物车项（根据商品ID和SKU ID）
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param skuId SKU ID（可选）
     * @return 购物车项
     */
    @Select("<script>" +
            "SELECT * FROM cart WHERE user_id = #{userId} AND product_id = #{productId} " +
            "<if test='skuId != null'>AND sku_id = #{skuId}</if>" +
            "AND is_deleted = 0 LIMIT 1" +
            "</script>")
    CartItem selectByUserAndProduct(@Param("userId") Long userId, 
                                     @Param("productId") Long productId,
                                     @Param("skuId") Long skuId);
    
    /**
     * 更新数量
     * 
     * @param id 购物车项ID
     * @param quantity 数量
     * @return 更新行数
     */
    @Update("UPDATE cart SET quantity = #{quantity}, subtotal = product_price * #{quantity}, " +
            "update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 更新选中状态
     * 
     * @param userId 用户ID
     * @param selected 选中状态
     * @return 更新行数
     */
    @Update("UPDATE cart SET selected = #{selected}, update_time = NOW() " +
            "WHERE user_id = #{userId} AND is_deleted = 0")
    int updateSelectedByUserId(@Param("userId") Long userId, @Param("selected") Integer selected);
}

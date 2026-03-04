package com.vibe.cart.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vibe.common.core.log.LogUtils;
import com.vibe.cart.entity.CartItem;
import com.vibe.cart.mapper.CartItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 购物车过期清理任务
 * 定时清理过期的购物车商品（超过30天未更新）
 * 
 * @author vibe
 * @date 2024-01-13
 */
@Slf4j
@Component
public class CartExpireTask {
    
    @Autowired
    private CartItemMapper cartItemMapper;
    
    /**
     * 清理过期购物车
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredCart() {
        log.info("开始清理过期购物车");
        LogUtils.businessLog("CART_EXPIRE", "清理过期购物车开始", LocalDateTime.now());
        
        // 清理30天前未更新的购物车项
        LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
        
        LambdaUpdateWrapper<CartItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.lt(CartItem::getUpdateTime, expireTime)
                .eq(CartItem::getIsDeleted, 0)
                .set(CartItem::getIsDeleted, 1)
                .set(CartItem::getUpdateTime, LocalDateTime.now());
        
        int deletedCount = cartItemMapper.update(null, updateWrapper);
        
        log.info("过期购物车清理完成，清理数量: {}", deletedCount);
        LogUtils.businessLog("CART_EXPIRE", "清理过期购物车完成", deletedCount);
    }
}

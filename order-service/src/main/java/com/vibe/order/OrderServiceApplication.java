package com.vibe.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 订单服务启动类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableCaching
@MapperScan("com.vibe.order.mapper")
public class OrderServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

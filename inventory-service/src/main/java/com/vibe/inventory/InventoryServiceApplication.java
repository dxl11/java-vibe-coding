package com.vibe.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存服务启动类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vibe.inventory.mapper")
public class InventoryServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}

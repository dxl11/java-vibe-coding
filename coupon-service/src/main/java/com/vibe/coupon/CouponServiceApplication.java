package com.vibe.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 优惠券服务启动类
 * 
 * @author vibe
 * @date 2024-01-13
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.vibe.coupon.mapper")
public class CouponServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CouponServiceApplication.class, args);
    }
}

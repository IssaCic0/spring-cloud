package com.baidu.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.baidu.api")
@MapperScan("com.baidu.shop.mapper")
@ComponentScan(basePackages = {"com.baidu.shop", "com.baidu.common", "com.baidu.security", "com.baidu.config"})
public class ShopApplication8083 {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication8083.class, args);
    }
}

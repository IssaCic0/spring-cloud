package com.baidu.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.baidu.api")
@MapperScan("com.baidu.user.mapper")
@ComponentScan(basePackages = {"com.baidu.user", "com.baidu.common", "com.baidu.security", "com.baidu.config"})
public class UserApplication8081 {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication8081.class, args);
    }
}

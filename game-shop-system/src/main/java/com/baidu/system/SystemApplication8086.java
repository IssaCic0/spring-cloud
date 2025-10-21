package com.baidu.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.baidu.api")
@MapperScan("com.baidu.system.mapper")
@ComponentScan(basePackages = {"com.baidu.system", "com.baidu.common", "com.baidu.security", "com.baidu.config", "com.baidu.utils"})
public class SystemApplication8086 {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication8086.class, args);
    }
}

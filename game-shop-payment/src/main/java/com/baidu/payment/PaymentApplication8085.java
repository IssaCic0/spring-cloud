package com.baidu.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.baidu.api")
@MapperScan("com.baidu.payment.mapper")
@ComponentScan(basePackages = {"com.baidu.payment", "com.baidu.common", "com.baidu.security", "com.baidu.config"})
public class PaymentApplication8085 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication8085.class, args);
    }
}

package com.baidu.api.user;

import com.baidu.common.ApiResponse;
import com.baidu.api.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "game-shop-user", path = "/api/accounts")
public interface UserFeignClient {
    
    @GetMapping("/{userId}")
    ApiResponse<UserDTO> getUserById(@PathVariable("userId") Long userId);
}


package com.baidu.user.controller;

import com.baidu.common.ApiResponse;
import com.baidu.user.entity.User;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.user.service.AccountService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 用户名密码登录
    @PostMapping("/login")
    public ApiResponse<Map<String,Object>> login(@RequestBody @Validated LoginRequest req){
        User u = accountService.login(req.username, req.password);
        Map<String,Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("nickname", u.getNickname());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("address", u.getAddress());
        m.put("role", u.getRole());
        m.put("enabled", u.getEnabled());
        return ApiResponse.ok(m);
    }

    // 买家注册
    @PostMapping("/register")
    public ApiResponse<Map<String,Object>> register(@RequestBody @Validated RegisterRequest req){
        Long id = accountService.register(req.username, req.password, req.email, req.phone);
        Map<String,Object> m = new HashMap<>();
        m.put("userId", id);
        return ApiResponse.ok(m);
    }

    // 商家注册
    @PostMapping("/register/merchant")
    public ApiResponse<Map<String,Object>> registerMerchant(@RequestBody @Validated RegisterRequest req){
        Long id = accountService.registerMerchant(req.username, req.password, req.email, req.phone);
        Map<String,Object> m = new HashMap<>();
        m.put("userId", id);
        return ApiResponse.ok(m);
    }

    // 管理员：查询全部用户
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin/users")
    public ApiResponse<Map<String, Object>> listAllUsers(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<User> p = accountService.adminList(page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", p.getNumber());
        resp.put("size", p.getSize());
        resp.put("total", p.getTotalElements());
        resp.put("items", p.getContent().stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("address", u.getAddress());
            m.put("role", u.getRole());
            m.put("enabled", u.getEnabled());
            return m;
        }).collect(Collectors.toList()));
        return ApiResponse.ok(resp);
    }

    // 管理员：封禁
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin/users/{id}/ban")
    public ApiResponse<Void> banUser(@PathVariable Long id, @RequestBody(required = false) BanRequest req) {
        accountService.adminBan(id, req == null ? null : req.reason);
        return ApiResponse.ok();
    }

    // 管理员：改角色
    @RequireRoles({Role.ADMIN})
    @PatchMapping("/admin/users/{id}/role")
    public ApiResponse<Void> changeRole(@PathVariable Long id, @RequestBody @Validated ChangeRoleRequest req) {
        Role role = Role.fromHeader(req.role);
        if (role == null) throw new IllegalArgumentException();
        accountService.adminChangeRole(id, role);
        return ApiResponse.ok();
    }

    // 管理员：新增用户
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin/users")
    public ApiResponse<Map<String, Object>> adminCreate(@RequestBody @Validated AdminCreateRequest req) {
        Role role = req.role == null ? Role.BUYER : Role.fromHeader(req.role);
        if (role == null) throw new IllegalArgumentException();
        Long id = accountService.adminCreate(req.username, req.password, req.nickname, req.email,
                req.phone, req.address, role, req.enabled);
        Map<String, Object> m = new HashMap<>();
        m.put("userId", id);
        return ApiResponse.ok(m);
    }

    // 管理员：编辑用户
    @RequireRoles({Role.ADMIN})
    @PutMapping("/admin/users/{id}")
    public ApiResponse<Void> adminUpdate(@PathVariable Long id, @RequestBody @Validated AdminUpdateRequest req) {
        Role role = req.role == null ? null : Role.fromHeader(req.role);
        if (req.role != null && role == null) throw new IllegalArgumentException();
        accountService.adminUpdate(id, req.password, req.nickname, req.email, req.phone, req.address, role, req.enabled);
        return ApiResponse.ok();
    }

    // 管理员：删除用户
    @RequireRoles({Role.ADMIN})
    @DeleteMapping("/admin/users/{id}")
    public ApiResponse<Void> adminDelete(@PathVariable Long id) {
        accountService.adminDelete(id);
        return ApiResponse.ok();
    }

    // Feign接口：根据ID获取用户
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable Long userId) {
        User user = accountService.getMe(userId);
        return ApiResponse.ok(user);
    }

    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class ChangeRoleRequest {
        @NotBlank
        public String role; // 角色（买家/商家/管理员）
    }

    public static class RegisterRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        @Email
        public String email;
        public String phone;
    }

    public static class BanRequest {
        public String reason;
    }

    public static class AdminCreateRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        public String nickname;
        @Email
        public String email;
        public String phone;
        public String address;
        public String role; // 可选（买家/商家/管理员）
        public Boolean enabled; // 可选（启用状态）
    }

    public static class AdminUpdateRequest {
        public String password; // 可选（新密码）
        public String nickname;
        @Email
        public String email;
        public String phone;
        public String address;
        public String role; // 可选（买家/商家/管理员）
        public Boolean enabled; // 可选（启用状态）
    }
}


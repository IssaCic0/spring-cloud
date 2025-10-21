package com.baidu.user.service;

import com.baidu.user.entity.User;
import com.baidu.security.Role;
import org.springframework.data.domain.Page;

public interface AccountService {

    Long register(String username, String password, String email, String phone);

    User login(String username, String password);

    Long registerMerchant(String username, String password, String email, String phone);

    void deleteMe(Long userId);

    void updateMe(Long userId, String nickname, String email, String phone, String address);

    User getMe(Long userId);

    Page<User> adminList(int page, int size);

    void adminBan(Long id, String reason);

    void adminChangeRole(Long id, Role role);

    Long adminCreate(String username, String password, String nickname, String email,
                     String phone, String address, Role role, Boolean enabled);

    void adminUpdate(Long id, String password, String nickname, String email,
                     String phone, String address, Role role, Boolean enabled);

    void adminDelete(Long id);
}


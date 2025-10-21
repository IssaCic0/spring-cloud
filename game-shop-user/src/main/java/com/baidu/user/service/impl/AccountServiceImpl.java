package com.baidu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baidu.user.mapper.UserMapper;
import com.baidu.user.entity.User;
import com.baidu.security.Role;
import com.baidu.user.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired private UserMapper userMapper;

    @Override
    @Transactional
    public Long register(String username, String password, String email, String phone) {
        User existed = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existed != null) throw new IllegalArgumentException("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(Role.BUYER);
        u.setEnabled(true);
        userMapper.insert(u);
        return u.getId();
    }

    @Override
    public User login(String username, String password) {
        User u = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (u == null) throw new NoSuchElementException("用户不存在");
        if (u.getPassword() == null || !u.getPassword().equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }
        return u;
    }

    @Override
    @Transactional
    public Long registerMerchant(String username, String password, String email, String phone) {
        User existed = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existed != null) throw new IllegalArgumentException("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(Role.MERCHANT);
        u.setEnabled(true);
        userMapper.insert(u);
        return u.getId();
    }

    @Override
    @Transactional
    public void deleteMe(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new NoSuchElementException("用户不存在");
        u.setEnabled(false);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void updateMe(Long userId, String nickname, String email, String phone, String address) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new NoSuchElementException("用户不存在");
        if (nickname != null) u.setNickname(nickname);
        if (email != null) u.setEmail(email);
        if (phone != null) u.setPhone(phone);
        if (address != null) u.setAddress(address);
        userMapper.updateById(u);
    }

    @Override
    public User getMe(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) throw new NoSuchElementException("用户不存在");
        return u;
    }

    @Override
    public Page<User> adminList(int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> mp =
                userMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1L, size),
                        new QueryWrapper<User>().orderByDesc("created_at"));
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    @Transactional
    public void adminBan(Long id, String reason) {
        User u = userMapper.selectById(id);
        if (u == null) throw new NoSuchElementException("用户不存在");
        u.setEnabled(false);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void adminChangeRole(Long id, Role role) {
        User u = userMapper.selectById(id);
        if (u == null) throw new NoSuchElementException("用户不存在");
        u.setRole(role);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public Long adminCreate(String username, String password, String nickname, String email,
                            String phone, String address, Role role, Boolean enabled) {
        User existed = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existed != null) throw new IllegalArgumentException("用户名已存在");
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setNickname(nickname);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(address);
        u.setRole(role == null ? Role.BUYER : role);
        u.setEnabled(enabled == null ? true : enabled);
        userMapper.insert(u);
        return u.getId();
    }

    @Override
    @Transactional
    public void adminUpdate(Long id, String password, String nickname, String email,
                            String phone, String address, Role role, Boolean enabled) {
        User u = userMapper.selectById(id);
        if (u == null) throw new NoSuchElementException("用户不存在");
        if (password != null && !password.isEmpty()) u.setPassword(password);
        if (nickname != null) u.setNickname(nickname);
        if (email != null) u.setEmail(email);
        if (phone != null) u.setPhone(phone);
        if (address != null) u.setAddress(address);
        if (role != null) u.setRole(role);
        if (enabled != null) u.setEnabled(enabled);
        userMapper.updateById(u);
    }

    @Override
    @Transactional
    public void adminDelete(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw new NoSuchElementException("用户不存在");
        userMapper.deleteById(id);
    }
}


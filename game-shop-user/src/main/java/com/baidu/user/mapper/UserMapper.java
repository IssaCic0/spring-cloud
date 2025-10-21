package com.baidu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baidu.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}


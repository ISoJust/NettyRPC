package com.yoe.remote;

import com.yoe.pojo.User;

import java.util.List;

public interface UserRemote {

    Object saveUser(User user);

    Object saveUsers(List<User> users);
}

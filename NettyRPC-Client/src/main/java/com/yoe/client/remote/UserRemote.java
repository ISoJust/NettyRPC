package com.yoe.client.remote;

import com.yoe.client.pojo.User;
import com.yoe.client.response.RpcResponse;

import java.util.List;

public interface UserRemote {

    RpcResponse saveUser(User user);

    RpcResponse saveUsers(List<User> users);
}

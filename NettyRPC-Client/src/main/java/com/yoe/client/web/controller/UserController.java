package com.yoe.client.web.controller;

import com.yoe.client.annotation.RemoteInvoke;
import com.yoe.client.pojo.User;
import com.yoe.client.remote.UserRemote;
import com.yoe.client.response.RpcResponse;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserController {

    @RemoteInvoke
    private UserRemote userRemote;//这个是代理对象


    public void saveUser(User user){
        RpcResponse response = userRemote.saveUser(user);

        System.out.println("获取到response" + response.toString());
    }


    public void saveUsers(List<User> userList){
        RpcResponse response = userRemote.saveUsers(userList);


    }
}

package com.yoe.server.remoteService.impl;

import com.yoe.server.annotation.RemoteService;
import com.yoe.server.pojo.User;
import com.yoe.server.remoteService.UserRemoteService;
import com.yoe.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@RemoteService
public class UserRemoteServiceImpl implements UserRemoteService {

    @Autowired
    private UserService userService;


    public void saveUser(User user){
        userService.save(user);
    }
}

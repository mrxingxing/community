package com.neu.community.util;

import com.neu.community.entity.User;
import org.springframework.stereotype.Component;
/**
 * 代替session持有用户信息，
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}

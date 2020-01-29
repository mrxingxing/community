package com.neu.community;

import com.neu.community.dao.DiscussPostMapper;
import com.neu.community.dao.UserMapper;
import com.neu.community.entity.DiscussPost;
import com.neu.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(1);
        System.out.println(user);
        user = userMapper.selectByName("SYSTEM");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("ORIKI");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("ORIKI@kamiyama.edu.jp");
        user.setHeaderUrl("nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(150,1);
        System.out.println(rows);
        rows=userMapper.updateHeader(150,"nowcoder.com/102.png");
        System.out.println(rows);
        rows=userMapper.updatePassword(150,"helloword");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts(){

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10);

        for(DiscussPost post:list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }
}

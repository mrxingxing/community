package com.neu.community;

import com.neu.community.dao.*;
import com.neu.community.entity.*;
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

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;

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

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,0,10,0);

        for(DiscussPost post:list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertPosts(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(153);
        discussPost.setType(1);
        discussPost.setTitle("宁愿输的是自己");
        discussPost.setContent("真的很难过");
        discussPost.setCreateTime(new Date(System.currentTimeMillis()));
        discussPost.setScore(100.0);
        discussPost.setStatus(0);
        int res = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(res);
    }

    @Test
    public void testInsertLoginTicket(){

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        loginTicketMapper.updateStatus("abc",1);
        loginTicket=loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectDiscussPostById(){
        System.out.println(discussPostMapper.selectDiscussPostById(281));
    }

    @Test
    public void testSelectCommentsByEntity(){
        List<Comment> res = commentMapper.selectCommentsByEntity(1,228,0,10);
        for(Comment comment:res){
            System.out.println(comment);
        }
    }

    @Test
    public void testSelectCountByEntity(){
        System.out.println( commentMapper.selectCountByEntity(1,228));
    }

    @Test
    public void testMessageMapper(){
        List<Message> list = messageMapper.selectConversations(153,0,10);
        for(Message msg :list){
            System.out.println(msg);
        }

        System.out.println(messageMapper.selectConversationCount(153));

        list = messageMapper.selectLetters("151_153",0,10);
        for(Message msg :list){
            System.out.println(msg);
        }
        System.out.println(messageMapper.selectLetterCount("151_153"));

        System.out.println(messageMapper.selectLetterUnreadCount(153,"151_153"));
    }
}

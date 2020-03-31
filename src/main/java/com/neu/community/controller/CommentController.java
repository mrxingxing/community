package com.neu.community.controller;

import com.neu.community.Event.EventProducer;
import com.neu.community.entity.*;
import com.neu.community.service.CommentService;
import com.neu.community.service.DiscussPostService;
import com.neu.community.service.LikeService;
import com.neu.community.service.UserService;
import com.neu.community.util.CommunityConstant;
import com.neu.community.util.CommunityUtil;
import com.neu.community.util.HostHolder;
import com.neu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.HtmlUtils;

import javax.websocket.server.PathParam;
import java.util.*;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/detail/commentDiscuss/{userId}",method = RequestMethod.GET)
    public String getUserComments(@PathVariable("userId") int userId, Model model, Page page){

        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/comment/detail/commentDiscuss/"+userId);
        int rows = commentService.findUserCommentsCount(userId);
        page.setRows(rows);

        List<Comment> commentsList = commentService.findUserComments(userId,page.getOffset(),page.getLimit());
        model.addAttribute("commentsCount",rows);

        List<Map<String,Object>> commentsMap = new ArrayList<>();
        for(Comment comment:commentsList){
            Map<String,Object> map = new HashMap<>();
            comment.setContent(HtmlUtils.htmlUnescape(comment.getContent()));
            map.put("comment",comment);
            map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId()));
            if(comment.getEntityType()==1){
                map.put("post",discussPostService.findDiscussPostById(comment.getEntityId()));
            }else if(comment.getEntityType()==2){
                int replyPostId=commentService.findCommentById(comment.getEntityId()).getEntityId();
                map.put("post",discussPostService.findDiscussPostById(replyPostId));
            }
            commentsMap.add(map);
        }

        model.addAttribute("commentsMap",commentsMap);
        return "/site/my-reply";
    }


    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);

        if(comment.getEntityType()==ENTITY_TYPE_POST){
             event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
             eventProducer.fireEvent(event);

            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,discussPostId);
        }

        return "redirect:/discuss/detail/"+discussPostId;
    }
}

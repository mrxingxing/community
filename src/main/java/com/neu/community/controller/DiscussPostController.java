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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"恁没登录嗷");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPosts(post);

        //kafka触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //报错统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(path = "/detail/postDiscuss/{userId}",method = RequestMethod.GET)
    public String getUserPosts(@PathVariable("userId") int userId,Model model,Page page){

        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/discuss/detail/postDiscuss/"+userId);
        int rows = discussPostService.findDiscussPostRows(userId);
        page.setRows(rows);

        List<DiscussPost> postList = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit());
        model.addAttribute("postCount",rows);

        List<Map<String,Object>> postMap = new ArrayList<>();
        for(DiscussPost post:postList){
            Map<String,Object> map = new HashMap<>();
            map.put("post",post);
            map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
            postMap.add(map);
        }

        model.addAttribute("postMap",postMap);
        return "/site/my-post";
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable ("discussPostId") int discussPostId, Model model,Page page){
        //贴子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeCount",likeCount);
        //点赞状态
        int likeStatus =
                hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);


        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        //评论VO列表
        List<Map<String,Object>> commentVolist = new ArrayList<>();
        if(commentList!=null){
            for(Comment comment : commentList){
                //评论map
                Map<String,Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //点赞数
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus =
                        hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(
                                hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply:replyList){
                        //回复map
                        Map<String,Object> replyVo = new HashMap<>();
                        //回复内容
                        replyVo.put("reply",reply);
                        //回复作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus =
                                hostHolder.getUser()==null?0:likeService.findEntityLikeStatus(
                                        hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        User target = reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVolist.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVolist);
        //待补充回复内容
        return "/site/discuss-detail";
    }

    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unTop",method = RequestMethod.POST)
    @ResponseBody
    public String setUnTop(int id){
        discussPostService.updateType(id,0);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unWonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setUnWonderful(int id){
        discussPostService.updateStatus(id,0);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        //触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


}

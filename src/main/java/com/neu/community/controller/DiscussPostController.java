package com.neu.community.controller;

import com.neu.community.Event.EventProducer;
import com.neu.community.entity.*;
import com.neu.community.service.*;
import com.neu.community.util.CommunityConstant;
import com.neu.community.util.CommunityUtil;
import com.neu.community.util.HostHolder;
import com.neu.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private FavoriteService favoriteService;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content,String labels){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"恁没登录嗷");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        if(StringUtils.isNotBlank(labels)){
            post.setLabel(labels);
        }
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

        //计算新帖分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        //报错统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    @RequestMapping(path = "/update",method = RequestMethod.POST)
    @ResponseBody
    public String updateContent(int postId,String content){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"恁没登录嗷");
        }
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if(post==null){
            return CommunityUtil.getJSONString(403,"帖子不存在或被删除了嗷");
        }
        if(post.getUserId()!=user.getId()){
            return CommunityUtil.getJSONString(403,"您不配改别人的帖子嗷");
        }
        discussPostService.updateContent(post.getId(),content);

        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());


        return CommunityUtil.getJSONString(0,"修改成功");

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

        List<DiscussPost> postList = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit(),0);
        model.addAttribute("postCount",rows);

        List<Map<String,Object>> postMap = new ArrayList<>();
        for(DiscussPost post:postList){
            if(post.getStatus()!=2){
                Map<String,Object> map = new HashMap<>();
                post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
                map.put("post",post);
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                postMap.add(map);
            }
        }

        model.addAttribute("postMap",postMap);
        return "/site/my-post";
    }
    @RequestMapping(path = "/detail/favorite/{userId}",method = RequestMethod.GET)
    public String getUserFavorite(@PathVariable("userId") int userId,Model model,Page page){

        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        User nowUser = hostHolder.getUser();
        if(nowUser.getId()!=user.getId()){
            throw new RuntimeException("无法查看他人收藏的帖子");
        }

        //用户基本信息
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/discuss/detail/favorite/"+userId);
        int rows = favoriteService.findUserFavoritePostCount(userId);
        page.setRows(rows);

        List<DiscussPost> postList = favoriteService.findUserFavoritePosts(userId,page.getLimit(),page.getOffset());
        model.addAttribute("postCount",rows);

        List<Map<String,Object>> postMap = new ArrayList<>();
        for(DiscussPost post:postList){
            if(post.getStatus()!=2){
                Map<String,Object> map = new HashMap<>();
                post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
                map.put("post",post);
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                postMap.add(map);
            }
        }

        model.addAttribute("postMap",postMap);
        return "/site/my-favorite";
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable ("discussPostId") int discussPostId, Model model,Page page){
        //贴子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);

        if(post.getStatus()==2){
            return "forward:/index";
        }

        post.setContent(HtmlUtils.htmlUnescape(post.getContent()));
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

        User nowUser = hostHolder.getUser();

        int favoriteStatus =
                favoriteService.findIsFavoritePost(nowUser==null?0:hostHolder.getUser().getId(),discussPostId);
        model.addAttribute("favoriteStatus",favoriteStatus);

        model.addAttribute("userId",nowUser==null?0:hostHolder.getUser().getId());



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
                comment.setContent(HtmlUtils.htmlUnescape(comment.getContent()));
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
        DiscussPost post = discussPostService.findDiscussPostById(id);
        discussPostService.updateType(id,1);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_WONDERFUL)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityUserId(post.getUserId())
                .setData("postTitle",post.getTitle())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unTop",method = RequestMethod.POST)
    @ResponseBody
    public String setUnTop(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        discussPostService.updateType(id,0);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_UNWONDERFUL)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityUserId(post.getUserId())
                .setData("postTitle",post.getTitle())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        discussPostService.updateStatus(id,1);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityUserId(post.getUserId())
                .setData("postTitle",post.getTitle())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //计算加精帖分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unWonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setUnWonderful(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        discussPostService.updateStatus(id,0);
        //同步到elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityUserId(post.getUserId())
                .setData("postTitle",post.getTitle())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //计算取消加精帖分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        DiscussPost post = discussPostService.findDiscussPostById(id);
        discussPostService.updateStatus(id,2);
        //触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityUserId(post.getUserId())
                .setData("postTitle",post.getTitle())
                .setData("postContent",post.getContent())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/favorite",method = RequestMethod.POST)
    @ResponseBody
    public String setFavorite(int userId,int entityId){

        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        User nowUser = hostHolder.getUser();
        if(userId!=nowUser.getId()){
            throw new RuntimeException("无法替他人收藏帖子");
        }

        favoriteService.addUserFavoritePosts(userId,entityId,1);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unFavorite",method = RequestMethod.POST)
    @ResponseBody
    public String setUnFavorite(int userId,int entityId){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        User nowUser = hostHolder.getUser();
        if(userId!=nowUser.getId()){
            throw new RuntimeException("无法替他人取消收藏帖子");
        }

        favoriteService.deleteUserFavoritePosts(userId,entityId);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/labels",method = RequestMethod.GET )
    @ResponseBody
    public String getLabels(){

        Set<String> set = discussPostService.findDiscussLabels();

        List<String> labels = new ArrayList<>(set);

        return CommunityUtil.toJSONString(labels);

    }
}

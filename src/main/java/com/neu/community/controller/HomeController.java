package com.neu.community.controller;

import com.neu.community.dao.DiscussPostMapper;
import com.neu.community.dao.elasticsearch.DiscussPostRepository;
import com.neu.community.entity.DiscussPost;
import com.neu.community.entity.Page;
import com.neu.community.entity.User;
import com.neu.community.service.DiscussPostService;
import com.neu.community.service.LikeService;
import com.neu.community.service.UserService;
import com.neu.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {


    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/",method = RequestMethod.GET)
    public String root(){
        return "forward:/index";
    }
    /*
    * 显示首页贴子
    * */
    @RequestMapping(path="/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page, @RequestParam(name="orderMode",defaultValue = "0")int orderMode ){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode);
        List<String> labelList = new ArrayList<>(discussPostService.findDiscussLabels());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                List<String> labels = new ArrayList<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                String[] arr =null;
                if(post.getLabel()!=null){
                    arr=post.getLabel().split(",");
                    for(String s :arr){
                        labels.add(s);
                    }
                }
                map.put("labels",labels);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("labelList",labelList);
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);

        return "index";
    }

    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }


}

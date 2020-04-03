package com.neu.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.neu.community.dao.DiscussPostMapper;
import com.neu.community.dao.LabelsMapper;
import com.neu.community.entity.DiscussPost;
import com.neu.community.entity.Labels;
import com.neu.community.util.RedisKeyUtil;
import com.neu.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private LabelsMapper labelsMapper;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-second}")
    private int expireSeconds;

    private LoadingCache<String, List<DiscussPost>> postListCache;

    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key==null||key.length()==0){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if(params==null||params.length!=2){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB");
                        List<DiscussPost> ans = discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                        List<DiscussPost> res = new ArrayList<>();
                        for(DiscussPost post : ans){
                            if(post.getStatus()!=2){
                                res.add(post);
                            }
                        }
                        return res;
                    }
                });
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB");

                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        if(userId==0&&orderMode==1){
            return postListCache.get(offset+":"+limit);
        }
        logger.debug("load post list from DB");
        List<DiscussPost> ans = discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
        List<DiscussPost> res = new ArrayList<>();
        for(DiscussPost post : ans){
            if(post.getStatus()!=2){
                res.add(post);
            }
        }
        return res;
    }

    public int findDiscussPostRows(int userId){
        if(userId==0){
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public Set<String> findDiscussLabels(){
        List<Labels> labelsList = labelsMapper.selectLabels();
        Set<String> labelsSet = new HashSet<>();
        for (Labels label : labelsList){
            labelsSet.add(new String(label.getLabel()));
        }
        return labelsSet;
    }

    public int addDiscussPosts(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数为空");
        }
        //去掉标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        //过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }

    public int updateContent(int id,String content){
        content = HtmlUtils.htmlEscape(content);
        content = sensitiveFilter.filter(content);
        return discussPostMapper.updateContent(id,content);
    }


}

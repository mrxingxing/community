package com.neu.community.quartz;

import com.neu.community.entity.DiscussPost;
import com.neu.community.service.DiscussPostService;
import com.neu.community.service.ElasticSearchService;
import com.neu.community.service.LikeService;
import com.neu.community.util.CommunityConstant;
import com.neu.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger= LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-01-26 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化时间失败");
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size()==0){
            logger.info("[任务取消] 没有需要刷新的帖子");
            return;
        }
        logger.info("[开始刷新] 正在刷新帖子分数:"+operations.size());
        while(operations.size()>0){
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕");

    }

    private void refresh(int postId){

        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post==null||post.getStatus()==POST_STATUS_DELETE){
            logger.error("该帖子不存在/帖子已被删除："+postId);
            return;
        }

        boolean isWonderful = post.getStatus()==1;
        int commentCount=post.getCommentCount();
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        double w = (isWonderful?75:0)+commentCount*10+likeCount*2;
        double score = Math.log10(Math.max(w,1))+(post.getCreateTime().getTime()-epoch.getTime())/(1000*3600*24);

        discussPostService.updateScore(postId,score);
        //更新score
        post.setScore(score);
        //同步eS
        elasticSearchService.saveDiscussPost(post);
    }
}

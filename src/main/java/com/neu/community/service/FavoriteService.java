package com.neu.community.service;

import com.neu.community.dao.DiscussPostMapper;
import com.neu.community.dao.FavoriteMapper;
import com.neu.community.entity.DiscussPost;
import com.neu.community.entity.Favorite;
import com.neu.community.entity.User;
import com.neu.community.util.HostHolder;
import com.neu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private HostHolder hostHolder;

    public int addUserFavoritePosts(int userId,int entityId,int entityType){
        User user = hostHolder.getUser();
        if(user.getId()!=userId){
            return -1;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setEntityId(entityId);
        favorite.setEntityType(entityType);
        favorite.setCreateTime(new Date());
        favorite.setStatus(0);

        return favoriteMapper.insertFavorite(favorite);
    }

    public int deleteUserFavoritePosts(int userId,int postId){
        return favoriteMapper.updateFavorite(userId, postId);
    }

    public List<DiscussPost> findUserFavoritePosts(int userId,int offset,int limit){
        List<Favorite> ans = favoriteMapper.selectUserFavoritesById(userId, limit, offset);
        List<DiscussPost> res = new ArrayList<>();
        for(Favorite favorite:ans){
            DiscussPost discussPost = discussPostMapper.selectDiscussPostById(favorite.getEntityId());
            if(discussPost!=null){
                res.add(discussPost);
            }
        }
        return res;
    }

    public int findUserFavoritePostCount(int userId){
        return favoriteMapper.selectUserFavoritesCount(userId);
    }

    public int findIsFavoritePost(int userId,int postId){
        User user = hostHolder.getUser();
        if(user==null){
            return 0;
        }
        if(user.getId()!=userId){
            return 0;
        }
        return favoriteMapper.isFavoritePost(userId, postId);
    }

    public int findPostFavoriteCount(int postId){
        return favoriteMapper.selectPostFavoriteCount(postId);
    }
}

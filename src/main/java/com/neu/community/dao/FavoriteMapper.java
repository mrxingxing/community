package com.neu.community.dao;

import com.neu.community.entity.Favorite;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "FavoriteMapper")
public interface FavoriteMapper {


    List<Favorite> selectUserFavoritesById(int userId,int offset,int limit);

    int selectUserFavoritesCount(int userId);

    int insertFavorite(Favorite favorite);

    int updateFavorite(int userId,int entityId);

    int isFavoritePost(int userId,int postId);

    int selectPostFavoriteCount(int postId);


}

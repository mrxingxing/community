package com.neu.community.dao;

import com.neu.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "DiscussPostMapper")
public interface DiscussPostMapper {


    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    int selectDiscussPostRows(@Param("userId") int UserId);


}
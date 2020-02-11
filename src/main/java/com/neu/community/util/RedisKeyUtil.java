package com.neu.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_FOLLOWEE = "followee";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

//    private static final String PREFIX_POST = "post";
//
//    private static final String PREFIX_COMMENT = "comment";



    //like:entity:entityType:entityId->set (userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //用户的赞
    //like:user:userId->int
    public static String getUSerLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //followee:userId:entityType->zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    //follower:entityType:entityId->zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    //登录验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }
    //post:userId->zset(post,createTime)
//    public static String getPostKey(int userId){
//        return PREFIX_POST+SPLIT+userId;
//    }
    //comment:userId->zset(comment,createTime)
//    public static String getCommentKey(int userId){
//        return PREFIX_COMMENT+SPLIT+userId;
//    }
}

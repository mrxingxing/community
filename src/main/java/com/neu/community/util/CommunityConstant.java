package com.neu.community.util;

public interface CommunityConstant {

    int ACTIVATION_SUCCESS=0;

    int ACTIVATION_REPEAT=1;

    int ACTIVATION_FAILURE=2;

    //默认超时时间 12小时
    int DEFAULT_EXPIRED_SECONDS=3600*12;
    //记住超时时间 100天
    int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    //帖子状态：删除
    int POST_STATUS_DELETE=2;

    //贴子
    int ENTITY_TYPE_POST=1;
    //评论
    int ENTITY_TYPE_COMMENT=2;
    //人
    int ENTITY_TYPE_USER=3;
    //事件主题评论
    String TOPIC_COMMENT="comment";
    //事件主题点赞
    String TOPIC_LIKE="like";
    //事件主题关注
    String TOPIC_FOLLOW="follow";
    //事件删除
    String TOPIC_DELETE="delete";
    //事件置顶
    String TOPIC_TOP="top";
    //事件取消置顶
    String TOPIC_UNTOP="unTop";
    //事件加精
    String TOPIC_WONDERFUL="wonderful";
    //事件取消加精
    String TOPIC_UNWONDERFUL="unWonderful";
    //SYSTEM的id
    int SYSTEM_USER_ID = 1;
    //事件发帖
    String TOPIC_PUBLISH = "publish";
    //普通用户权限
    String AUTHORITY_USER = "user";
    //管理员
    String AUTHORITY_ADMIN = "admin";
    //版主
    String AUTHORITY_MODERATOR = "moderator";
    //分享
    String TOPIC_SHARE = "share";


}

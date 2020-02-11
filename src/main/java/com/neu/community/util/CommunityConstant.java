package com.neu.community.util;

public interface CommunityConstant {

    int ACTIVATION_SUCCESS=0;

    int ACTIVATION_REPEAT=1;

    int ACTIVATION_FAILURE=2;

    //默认超时时间 12小时
    int DEFAULT_EXPIRED_SECONDS=3600*12;
    //记住超时时间 100天
    int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    //贴子
    int ENTITY_TYPE_POST=1;
    //评论
    int ENTITY_TYPE_COMMENT=2;
    //人
    int ENTITY_TYPE_USER=3;

}

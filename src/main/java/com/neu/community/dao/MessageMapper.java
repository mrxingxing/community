package com.neu.community.dao;

import com.neu.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component(value = "MessageMapper")
public interface MessageMapper {

    List<Message> selectConversations(int userId,int offset,int limit);

    int selectConversationCount(int userId);

    List<Message>selectLetters(String conversationId,int offset,int limit);

    int selectLetterCount(String conversationId);

    int selectLetterUnreadCount(int userId,String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

    //查询最新的通知
    Message selectLatestNotice(int userId,String topic);
    //查询某个主题所包含的通知的数量
    int selectNoticeCount(int userId,String topic);
    //未读的通知的数量
    int selectNoticeUnreadCount(int userId,String topic);

    //查询某个主题包含的通知数量
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}

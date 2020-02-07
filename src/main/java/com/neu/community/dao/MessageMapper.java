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


}

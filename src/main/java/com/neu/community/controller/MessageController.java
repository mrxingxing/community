package com.neu.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.neu.community.entity.Message;
import com.neu.community.entity.Page;
import com.neu.community.entity.User;
import com.neu.community.service.MessageService;
import com.neu.community.service.UserService;
import com.neu.community.util.CommunityConstant;
import com.neu.community.util.CommunityUtil;
import com.neu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){

        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList!=null){
            for(Message message:conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId()==message.getFromId()?message.getToId():message.getFromId();
                map.put("target",userService.findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("target",getLetterTarget(conversationId));

        List<Integer> ids =getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList!=null){
            for(Message message:letterList){
                if(hostHolder.getUser().getId()==message.getToId()&&message.getStatus()==0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    private User getLetterTarget(String conversationId){
        String[] ids=conversationId.split("_");

        int d0=Integer.parseInt(ids[0]);
        int d1=Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId()==d0){
            return userService.findUserById(d1);
        }else {
            return userService.findUserById(d0);
        }
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findUserByName(toName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()){
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int letterId){
        if(hostHolder.getUser()==null){
            return CommunityUtil.getJSONString(1,"删除失败，已被删除或恁未登录");
        }
        List<Integer> ids = new ArrayList<>();
        ids.add(letterId);
        messageService.deleteMessage(ids);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(),TOPIC_COMMENT);
        if(message!=null){
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());

            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);

            messageVO.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_COMMENT);

            messageVO.put("unread",unread);

            model.addAttribute("commentNotice",messageVO);
        }

        //查询点赞通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_LIKE);
        if(message!=null){

            Map<String,Object> messageVO2 = new HashMap<>();
            messageVO2.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());

            Map<String,Object> data2 = JSONObject.parseObject(content,HashMap.class);
            messageVO2.put("user",userService.findUserById((int)data2.get("userId")));
            messageVO2.put("entityType",data2.get("entityType"));
            messageVO2.put("entityId",data2.get("entityId"));
            messageVO2.put("postId",data2.get("postId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);

            messageVO2.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_LIKE);

            messageVO2.put("unread",unread);

            model.addAttribute("likeNotice",messageVO2);
        }

        //查询关注通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_FOLLOW);
        if(message!=null){
            Map<String,Object> messageVO3 = new HashMap<>();
            messageVO3.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());

            Map<String,Object> data3 = JSONObject.parseObject(content,HashMap.class);
            messageVO3.put("user",userService.findUserById((int)data3.get("userId")));
            messageVO3.put("entityType",data3.get("entityType"));
            messageVO3.put("entityId",data3.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);

            messageVO3.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_FOLLOW);

            messageVO3.put("unread",unread);

            model.addAttribute("followNotice",messageVO3);
        }

        //查询删帖类通知
        message = messageService.findLatestNotice(user.getId(),TOPIC_DELETE);
        if(message!=null){
            Map<String,Object> messageVO4 = new HashMap<>();
            messageVO4.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());

            Map<String,Object> data4 = JSONObject.parseObject(content,HashMap.class);
            messageVO4.put("postTitle",data4.get("postTitle"));
            messageVO4.put("postContent",HtmlUtils.htmlUnescape((String) data4.get("postContent")));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_DELETE);

            messageVO4.put("count",count);

            int unread = messageService.findNoticeUnreadCount(user.getId(),TOPIC_DELETE);

            messageVO4.put("unread",unread);

            model.addAttribute("deleteNotice",messageVO4);
        }

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        //查询未读通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Page page,Model model){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        List<Message> noticeList = messageService.findNotices(user.getId(),topic,page.getOffset(),page.getLimit());
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList!=null){
            for(Message notice : noticeList){
                Map<String,Object> map = new HashMap<>();
                map.put("notice",notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
                if(data.containsKey("userId")){
                    map.put("user",userService.findUserById((Integer)data.get("userId")));
                }
                if(data.containsKey("entityType")){
                    map.put("entityType",data.get("entityType"));
                }
                if(data.containsKey("entityId")){
                    map.put("entityId",data.get("entityId"));
                }
                if(data.containsKey("postId")){
                    map.put("postId",data.get("postId"));
                }
                if(data.containsKey("postTitle")){
                    map.put("postTitle",data.get("postTitle"));
                }
                if(data.containsKey("postContent")){
                    map.put("postContent",data.get("postContent"));
                }
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);




        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
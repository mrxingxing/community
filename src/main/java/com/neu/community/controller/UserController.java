package com.neu.community.controller;

import com.neu.community.annotation.LoginRequired;
import com.neu.community.entity.User;
import com.neu.community.service.FollowService;
import com.neu.community.service.LikeService;
import com.neu.community.service.UserService;
import com.neu.community.util.CommunityConstant;
import com.neu.community.util.CommunityUtil;
import com.neu.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        //生成上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "site/setting";
    }

    //更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空");
        }
        String url = headerBucketUrl+"/"+fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    //已重构
    /*
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","恁还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式8正确");
            return "/site/setting";
        }

        fileName = CommunityUtil.generateUUID()+"."+suffix;
        File dest = new File(uploadPath+'/'+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }
        //更新当前用户头像的路径
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }
    */


    //已重构
    /*
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        fileName=uploadPath+"/"+fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        response.setContentType("image/"+suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }
    */

    @LoginRequired
    @RequestMapping(path = "/resetPassword",method = RequestMethod.POST)
    public String Register(Model model, String oldPassword,String newPassword,String confirmPassword,@CookieValue("ticket")String ticket){
        User user = hostHolder.getUser();
        if(user==null){
            model.addAttribute("oldPasswordMsg","未检测到登录信息");
            return "/site/setting";
        }
        if(!confirmPassword.equals(newPassword)){
            model.addAttribute("confirmPasswordMsg","两次输入的密码不一致");
            return "/site/setting";
        }
        if(oldPassword.equals(newPassword)){
            model.addAttribute("newPasswordMsg","旧密码不能与新密码相同");
            return "/site/setting";
        }
        Map<String,Object> map = userService.resetPassword(user.getId(),oldPassword,newPassword);
        if(map.get("successMsg")!=null){
            userService.logout(ticket);
            model.addAttribute("msg","修改成功，请重新登录");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else{
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        //用户基本信息
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

}

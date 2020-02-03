package com.neu.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static boolean isStrongPassword(String password){
        if(StringUtils.isBlank(password)){
            return false;
        }
        if(password.length()<8){
            return false;
        }


        int lowerCase=0;
        int upperCase=0;
        int num=0;
        boolean threeConsecutive=false;

        for (int i = 0; i < password.length() ; i++) {
            if(Character.isLowerCase(password.charAt(i))){
                lowerCase=1;
            }
            if(Character.isUpperCase(password.charAt(i))){
                upperCase=1;
            }
            if(Character.isDigit(password.charAt(i))){
                num=1;
            }
            if(i+2<password.length()&&password.charAt(i)==password.charAt(i+1)&&password.charAt(i+1)==password.charAt(i+2)){
                threeConsecutive=true;
            }
        }
        if(threeConsecutive==true){
            return false;
        }
        return lowerCase+upperCase+num==3;
    }


}

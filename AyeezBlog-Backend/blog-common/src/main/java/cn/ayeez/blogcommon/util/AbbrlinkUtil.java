package cn.ayeez.blogcommon.util;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.UUID;


public class AbbrlinkUtil {
    public static String getShortUrl() {
        String abbrlink;
        //生成uuid
        UUID uuid = UUID.randomUUID();
        System.out.println("生成的UUID："+ uuid);
//        尝试用SHA-256生成UUID的哈希值
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uuid.toString().getBytes());
//            将哈希值转换为16进制字符串
            StringBuilder shortLink = new StringBuilder();
            for (byte b : hash){
                shortLink.append(String.format("%02x",b));
            }
//            截取前6位作为短链接
            abbrlink = shortLink.substring(0,6);
        }catch (Exception e){
            throw new RuntimeException("哈希值生成失败",e);
        }
        return abbrlink;
    }


}
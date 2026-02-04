package com.kardan;

import java.security.MessageDigest;

public class HashUtil {
    public static String hash(String text){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes= md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte b : bytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }catch (Exception e){throw new RuntimeException(e);}
    }
}

package com.kardan;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        }catch (NoSuchAlgorithmException e){
            System.out.println("Algorithm not found!");
            return "";
        }
    }
}

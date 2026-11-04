package org.example.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashedPasswordUtils {
    public static String hashedPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
    public static boolean checkHashedPassword(String password, String hashed){
        try{
            return BCrypt.checkpw(password, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}

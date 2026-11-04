package org.example.service;

import org.example.model.User;

public interface AuthService {
    User login(String username, String password);
    User forgetPassword(String email, String newPassword);
    boolean register(String fullname,String username,  String email, String password, String checkPasword);
}

package org.example.service.impl;

import org.example.constant.ErrorMessage;
import org.example.dao.LoginDAO;
import org.example.dao.RegisterDAO;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.utils.AlertUtils;
import org.example.utils.HashedPasswordUtils;

public class AuthServiceImpl implements AuthService {

    private LoginDAO loginDAO = new LoginDAO();
    private RegisterDAO registerDAO = new RegisterDAO();

    @Override
    public User login(String username, String password) {
        User user = loginDAO.getUserByUsername(username);

        if(user != null && HashedPasswordUtils.checkHashedPassword(password, user.getPassword())){
            return user;
        }
        return null;
    }

    @Override
    public User forgetPassword(String email, String newPassword) {
        User checkUser = loginDAO.getUserByEmail(email);
        if(checkUser != null){
            String hashNewPass = HashedPasswordUtils.hashedPassword(newPassword);
            checkUser.setPassword(hashNewPass);
            loginDAO.updateUser(checkUser);
        }

        return checkUser;
    }

    @Override
    public boolean register(String fullname, String username, String email, String password, String checkPasword) {
        User checkUser = loginDAO.getUserByUsername(username);

        if(checkUser != null){
            System.out.println(ErrorMessage.username);
            AlertUtils.showFailAlert(ErrorMessage.fail, ErrorMessage.username);
            return false;
        }
        if(!checkPasword.equals(password)){
            System.out.println(ErrorMessage.checkPassword);
            AlertUtils.showFailAlert(ErrorMessage.fail, ErrorMessage.checkPassword);
            return false;
        }
        User checkEmail = loginDAO.getUserByEmail(email);
        if(checkEmail != null){
            System.out.println(ErrorMessage.checkEmail);
            AlertUtils.showFailAlert(ErrorMessage.fail, ErrorMessage.checkEmail);
            return false;
        }

        User newUser = new User();
        newUser.setFullname(fullname);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);

        String hashPass = HashedPasswordUtils.hashedPassword(password);
        newUser.setPassword(hashPass);
        return registerDAO.registerNewUser(newUser);
    }
}

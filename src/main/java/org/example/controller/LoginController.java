package org.example.controller;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.constant.ErrorMessage;
import org.example.dao.LoginDAO;
import org.example.model.User;
import org.example.model.UserSession;
import org.example.service.impl.AuthServiceImpl;
import org.example.utils.AlertUtils;
import org.example.utils.ChangeFXML;

public class LoginController {
    @FXML private TextField usernameLG;
    @FXML private PasswordField passwordLG;
    @FXML private TextField passwordTextLG;
    @FXML private TextField emailFG;
    @FXML private ImageView iconEYE;

    private Image closeEYE = new Image(getClass().getResourceAsStream("/assets/images/icon/closeEye.png"));
    private Image openEYE = new Image(getClass().getResourceAsStream("/assets/images/icon/openEye.png"));

    @FXML private Label errorUsername;
    @FXML private Label errorPassword;

    private LoginDAO loginDAO = new LoginDAO();
    private AuthServiceImpl authService = new AuthServiceImpl();

    @FXML
    public void initialize() {
        passwordTextLG.setVisible(false);
        iconEYE.setImage(closeEYE);

        // Đồng bộ 2 ô mật khẩu khi nhập liệu
        passwordLG.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.equals(passwordTextLG.getText())) {
                passwordTextLG.setText(newV);
            }
        });
        passwordTextLG.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !newV.equals(passwordLG.getText())) {
                passwordLG.setText(newV);
            }
        });
    }

    @FXML
    public void loginLG(ActionEvent event) {
        resetErrorEmpty();
        String name = usernameLG.getText() == null ? "" : usernameLG.getText().trim();
        String pass = passwordTextLG.isVisible() ? passwordTextLG.getText() : passwordLG.getText();
        if (pass == null) pass = "";

        boolean checkLG = false;
        if (name.isEmpty()) {
            errorUsername.setText("Tên đăng nhập không được để trống!");
            checkLG = true;
        }
        if (pass.isEmpty()) {
            errorPassword.setText("Mật khẩu không được để trống!");
            checkLG = true;
        }
        if (checkLG) {
            return;
        } else {
            User user = authService.login(name, pass);
            if (user != null) {
                UserSession.login(user);
                ChangeFXML.changeFXML(event, "/view/home.fxml");
            } else {
                AlertUtils.showFailAlert(ErrorMessage.fail, ErrorMessage.failLogin);
                return;
            }
        }
    }
    @FXML
    public void showPassword(Event event){
        if(!passwordTextLG.isVisible()){
            passwordTextLG.setText(passwordLG.getText());
            passwordTextLG.setVisible(true);
            passwordLG.setVisible(false);
            iconEYE.setImage(openEYE);
        }
        else{
            passwordLG.setText(passwordTextLG.getText());
            passwordTextLG.setVisible(false);
            passwordLG.setVisible(true);
            iconEYE.setImage(closeEYE);
        }
    }
    @FXML
    public void forgetLG(ActionEvent event){
        ChangeFXML.changeFXML(event, "/view/forgetPassword.fxml");
    }
    @FXML
    public void registerLG(ActionEvent event){
        ChangeFXML.changeFXML(event, "/view/register.fxml");
    }
    private void resetErrorEmpty(){
        errorUsername.setText("");
        errorPassword.setText("");
    }
}

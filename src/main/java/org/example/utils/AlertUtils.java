package org.example.utils;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static void showFailAlert(String equal, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(equal);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static void showSuccessAlert(String equal, String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(equal);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

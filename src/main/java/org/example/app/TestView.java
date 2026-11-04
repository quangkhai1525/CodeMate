package org.example.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.utils.DBConnection;

import java.net.URL;
import java.util.Objects;

public class TestView extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = Objects.requireNonNull(getClass().getResource("/view/login.fxml"));
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root);
        stage.setTitle("CodeMate");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DBConnection.closeEntityManager();
    }
    public static void main(String[] args){
        launch(args);
    }
}

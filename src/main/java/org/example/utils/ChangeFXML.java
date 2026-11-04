package org.example.utils;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ChangeFXML {
    public static void changeFXML(Event event, String pathFXML) {
        try {
            URL resource = ChangeFXML.class.getResource(pathFXML);
            if (resource == null) {
                System.err.println("Không tìm thấy file FXML tại: " + pathFXML);
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

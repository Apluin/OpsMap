package com.kardan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 600);

        stage.setTitle("OPSMAP");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

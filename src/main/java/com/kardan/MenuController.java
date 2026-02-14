package com.kardan;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {

    private boolean isSignUpMode;

    @FXML private Button signIn;
    @FXML private Button signUp;
    @FXML private Button exit;

    @FXML private VBox buttonBox;
    @FXML private VBox loginBox;

    @FXML private TextField username;
    @FXML private PasswordField password;

    @FXML private Button backBtn;
    @FXML private Button confirmBtn;

    @FXML private Label errorMsg;

    @FXML
    public void initialize() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);

        exit.setOnAction(e -> System.exit(0));
        signIn.setOnAction(e -> {
            showLogin(); isSignUpMode = false;});
        signUp.setOnAction(e -> {
            showLogin(); isSignUpMode = true;});

        username.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN , ENTER -> password.requestFocus();
            }
        });
        password.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP -> username.requestFocus();
                case ENTER -> confirmBtn.requestFocus();
            }
        });

        backBtn.setOnAction(e -> {

            loginBox.setVisible(false);
            loginBox.setManaged(false);
            username.clear();
            password.clear();
            errorMsg.setText("");

            buttonBox.setVisible(true);
            buttonBox.setManaged(true);
        });

        confirmBtn.setOnAction(e -> {
            String u = username.getText().trim();
            String p = password.getText().trim();

            if (u.isEmpty() || p.isEmpty()) {
                errorMsg.setText("Fill all fields");
                return;
            }

            if (isSignUpMode) {
                if (signUpUser(u, HashUtil.hash(p))) {
                    goToMainView();
                }
            } else {
                if (checkLogin(u, HashUtil.hash(p))) {
                    goToMainView();
                } else {
                    errorMsg.setText("Wrong username or password");
                }
            }
        });

    }

    private void showLogin() {
        buttonBox.setVisible(false);
        buttonBox.setManaged(false);

        loginBox.setVisible(true);
        loginBox.setManaged(true);
    }

    private boolean signUpUser(String user, String pass) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (var c = Database.connect();
             var p = c.prepareStatement(sql)) {

            p.setString(1, user);
            p.setString(2, pass);
            p.executeUpdate();
            return true;
        } catch (Exception e) {
            errorMsg.setText("Username already exists");
            return false;
        }
    }

    private boolean checkLogin(String user, String pass) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (var c = Database.connect();
             var p = c.prepareStatement(sql)) {

            p.setString(1, user);
            p.setString(2, pass);

            return p.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }

    private void goToMainView(){
        try {
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/mainView.fxml"));
            Stage stage = (Stage) confirmBtn.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1500, 800);

            MainController controller = loader.getController();
            controller.setUsername(username.getText().trim());
            controller.connect();

            stage.setScene(scene);
        }catch (IOException e){errorMsg.setText("Can not load main view!");}
    }
}


package com.kardan;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MenuController {

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
        signIn.setOnAction(e -> showLogin());
        signUp.setOnAction(e -> showLogin());

        username.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN -> password.requestFocus();
            }
        });

        password.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP -> username.requestFocus();
                case DOWN -> backBtn.requestFocus();
            }
        });

        backBtn.setOnAction(e -> {
            loginBox.setVisible(false);
            loginBox.setManaged(false);

            buttonBox.setVisible(true);
            buttonBox.setManaged(true);
        });

        confirmBtn.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                errorMsg.setText("Please fill all fields!");
            } else {
                errorMsg.setText("hello");
            }
        });
    }

    private void showLogin() {
        buttonBox.setVisible(false);
        buttonBox.setManaged(false);

        loginBox.setVisible(true);
        loginBox.setManaged(true);
    }
}


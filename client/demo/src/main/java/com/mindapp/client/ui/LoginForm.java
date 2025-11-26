package com.mindapp.client.ui;

import com.mindapp.client.api.ApiClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginForm {

    public void show(Stage stage) {
        stage.setTitle("MindApp - Вхід");

        Label lblTitle = new Label("Вітаємо в MindApp");
        lblTitle.setFont(new Font("Arial", 24));

        // Поле Логіну
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Логін (напр. user1)");
        txtUsername.setMaxWidth(250);

        // Поле Пароля (Нове)
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Пароль");
        txtPassword.setMaxWidth(250);

        Button btnLogin = new Button("Увійти");
        btnLogin.setDefaultButton(true); // Працює по Enter
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        
        btnLogin.setOnAction(e -> {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();

            if (isValid(username, password)) {
                // Переходимо до головної форми
                new MainForm(username).show(stage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Введіть логін!").show();
            }
        });
        Button btnRegister = new Button("Немає акаунту? Зареєструватись");
        btnRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: blue; -fx-underline: true;");
        btnRegister.setOnAction(e -> new RegisterForm().show(stage));

        btnLogin.setOnAction(e -> {
            String u = txtUsername.getText();
            String p = txtPassword.getText();
            // Викликаємо справжній API
            if (new ApiClient().login(u, p)) {
                new MainForm(u).show(stage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Невірний логін або пароль").show();
            }
        });

        VBox root = new VBox(15, lblTitle, txtUsername, txtPassword, btnLogin, btnRegister);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 400, 350));
        stage.show();
    }

    // Проста валідація (для курсової цього достатньо)
    private boolean isValid(String user, String pass) {
        return !user.isEmpty(); // Головне, щоб був логін
    }
}
package com.mindapp.client.ui;

import com.mindapp.client.api.ApiClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterForm {
    private final ApiClient apiClient = new ApiClient();

    public void show(Stage stage) {
        stage.setTitle("MindApp - Реєстрація");

        Label lblTitle = new Label("Створити акаунт");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField txtUser = new TextField(); txtUser.setPromptText("Логін");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Пароль");

        Button btnReg = new Button("Зареєструватися");
        Button btnBack = new Button("Назад до входу");

        btnReg.setOnAction(e -> {
            if (apiClient.register(txtUser.getText(), txtPass.getText())) {
                new Alert(Alert.AlertType.INFORMATION, "Успішно! Тепер увійдіть.").showAndWait();
                new LoginForm().show(stage);
            } else {
                new Alert(Alert.AlertType.ERROR, "Помилка реєстрації (логін зайнятий?)").show();
            }
        });

        btnBack.setOnAction(e -> new LoginForm().show(stage));

        VBox root = new VBox(10, lblTitle, txtUser, txtPass, btnReg, new Separator(), btnBack);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 350, 300));
    }
}
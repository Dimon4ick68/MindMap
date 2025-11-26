package com.mindapp.client;

import com.mindapp.client.ui.LoginForm; // Імпортуємо логін

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) {
        new LoginForm().show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
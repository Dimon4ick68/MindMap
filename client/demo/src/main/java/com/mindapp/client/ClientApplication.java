package com.mindapp.client;

import com.mindapp.client.ui.MainForm;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Запускаємо головну форму
        new MainForm().show(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
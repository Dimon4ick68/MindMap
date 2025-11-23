package com.mindapp.client.ui;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class EditorForm {
    private final MindMap map;
    private final ApiClient apiClient = new ApiClient();

    public EditorForm(MindMap map) {
        this.map = map;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Редактор: " + map.getTitle());

        // Поле для назви
        TextField titleField = new TextField(map.getTitle());
        
        // Кнопка збереження
        Button btnSave = new Button("Зберегти у Хмару");
        btnSave.setOnAction(e -> {
            map.setTitle(titleField.getText());
            saveMap();
        });

        // Тут ми пізніше (в курсовій) будемо малювати Canvas і вузли
        TextArea jsonPreview = new TextArea("Тут буде візуалізація мапи...");
        jsonPreview.setEditable(false);

        HBox topPanel = new HBox(10, new Label("Назва:"), titleField, btnSave);
        topPanel.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(jsonPreview);

        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void saveMap() {
        try {
            apiClient.saveMap(map);
            new Alert(Alert.AlertType.INFORMATION, "Мапу успішно збережено!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Помилка збереження: " + e.getMessage()).show();
        }
    }
}
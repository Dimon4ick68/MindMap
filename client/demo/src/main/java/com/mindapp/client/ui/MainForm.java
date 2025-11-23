package com.mindapp.client.ui;

import java.util.List;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainForm {
    private final ApiClient apiClient = new ApiClient();
    // Хардкод юзера для лаби, пізніше тут буде форма логіну
    private final String currentUserId = "user1"; 

    public void show(Stage stage) {
        stage.setTitle("MindApp - Мої Мапи");

        // Таблиця
        TableView<MindMap> table = new TableView<>();
        TableColumn<MindMap, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<MindMap, String> titleCol = new TableColumn<>("Назва");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        table.getColumns().addAll(idCol, titleCol);

        // Кнопки
        Button btnCreate = new Button("Створити нову мапу");
        btnCreate.setOnAction(e -> openEditor(new MindMap("Нова мапа", currentUserId)));

        Button btnRefresh = new Button("Оновити список");
        btnRefresh.setOnAction(e -> loadMaps(table));

        Button btnOpen = new Button("Відкрити обрану");
        btnOpen.setOnAction(e -> {
            MindMap selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openEditor(selected);
        });

        VBox root = new VBox(10, btnCreate, btnOpen, btnRefresh, table);
        root.setPadding(new Insets(15));

        stage.setScene(new Scene(root, 600, 400));
        stage.show();

        // Завантажуємо дані при старті
        loadMaps(table);
    }

    private void loadMaps(TableView<MindMap> table) {
        try {
            List<MindMap> maps = apiClient.getMaps(currentUserId);
            table.setItems(FXCollections.observableArrayList(maps));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage()).show();
        }
    }

    private void openEditor(MindMap map) {
        new EditorForm(map).show();
    }
}
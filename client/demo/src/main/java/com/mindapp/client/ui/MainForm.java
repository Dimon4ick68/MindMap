package com.mindapp.client.ui;

import java.util.List;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainForm {
    private final ApiClient apiClient = new ApiClient();
    private final String currentUserId = "user1";
    
    private TabPane tabPane; // Головний контейнер вкладок

    public void show(Stage stage) {
        stage.setTitle("MindApp - Mind Mapping Software");

        // Створюємо TabPane
        tabPane = new TabPane();

        // Вкладка 1: Список мап (Dashboard)
        Tab dashboardTab = new Tab("Мої Мапи");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(createDashboard());
        
        tabPane.getTabs().add(dashboardTab);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    // Створює вміст вкладки "Список мап"
    private VBox createDashboard() {
        TableView<MindMap> table = new TableView<>();
        TableColumn<MindMap, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<MindMap, String> titleCol = new TableColumn<>("Назва");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        table.getColumns().addAll(idCol, titleCol);

        Button btnCreate = new Button("Створити нову мапу");
        btnCreate.setOnAction(e -> openMapInTab(new MindMap("Нова мапа", currentUserId)));

        Button btnRefresh = new Button("Оновити список");
        btnRefresh.setOnAction(e -> loadMaps(table));

        Button btnOpen = new Button("Відкрити обрану");
        btnOpen.setOnAction(e -> {
            MindMap selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openMapInTab(selected);
        });
        
        loadMaps(table); // Завантажити одразу

        VBox vbox = new VBox(10, btnCreate, btnOpen, btnRefresh, table);
        vbox.setPadding(new Insets(15));
        return vbox;
    }

    // ВІДКРИВАЄ РЕДАКТОР У НОВІЙ ВКЛАДЦІ!
    private void openMapInTab(MindMap map) {
        Tab mapTab = new Tab(map.getTitle());
        
        // Створюємо EditorForm, але не робимо show(), а беремо його Content
        EditorForm editor = new EditorForm(map);
        mapTab.setContent(editor.createContent()); // Ми змінимо EditorForm, щоб він повертав Node
        
        tabPane.getTabs().add(mapTab);
        tabPane.getSelectionModel().select(mapTab);
    }

    private void loadMaps(TableView<MindMap> table) {
        try {
            List<MindMap> maps = apiClient.getMaps(currentUserId);
            table.setItems(FXCollections.observableArrayList(maps));
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage()).show();
        }
    }
}
package com.mindapp.client.ui;

import java.util.List;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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
    private final String currentUserId;
    
    private TabPane tabPane;
    private Stage primaryStage; // 1. Винесли змінну сюди (на рівень класу)

    public MainForm(String userId) {
        this.currentUserId = userId;
    }

    public void show(Stage stage) {
        this.primaryStage = stage; // 2. Запам'ятовуємо stage при старті
        
        stage.setTitle("MindApp - Користувач: " + currentUserId);

        tabPane = new TabPane();

        Tab dashboardTab = new Tab("Мої Мапи");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(createDashboard());
        
        tabPane.getTabs().add(dashboardTab);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    private VBox createDashboard() {
        TableView<MindMap> table = new TableView<>();
        
        // --- КОЛОНКА "№" (Замість ID) ---
        TableColumn<MindMap, String> numCol = new TableColumn<>("№");
        numCol.setSortable(false);
        numCol.setPrefWidth(50);
        numCol.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(table.getItems().indexOf(column.getValue()) + 1 + ""));
        
        TableColumn<MindMap, String> titleCol = new TableColumn<>("Назва мапи");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(400);

        table.getColumns().addAll(numCol, titleCol);

        // Контекстне меню (Видалити)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("❌ Видалити мапу");
        deleteItem.setOnAction(e -> {
            MindMap selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteMap(selected, table);
            }
        });
        contextMenu.getItems().add(deleteItem);
        table.setContextMenu(contextMenu);

        // Кнопки
        Button btnCreate = new Button("Створити нову мапу");
        btnCreate.setOnAction(e -> openMapInTab(new MindMap("Нова мапа", currentUserId)));

        Button btnRefresh = new Button("Оновити список");
        btnRefresh.setOnAction(e -> loadMaps(table));

        Button btnOpen = new Button("Відкрити обрану");
        btnOpen.setOnAction(e -> {
            MindMap selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openMapInTab(selected);
        });

        // Кнопка виходу
        Button btnLogout = new Button("🚪 Вийти з акаунту");
        btnLogout.setStyle("-fx-background-color: #ffcccc;");
        btnLogout.setOnAction(e -> new LoginForm().show(primaryStage)); // 3. Використовуємо збережений stage

        
        loadMaps(table);

        VBox vbox = new VBox(10, btnCreate, btnOpen, btnRefresh, btnLogout, table);
        vbox.setPadding(new Insets(15));
        return vbox;
    }

    private void openMapInTab(MindMap map) {
        Tab mapTab = new Tab(map.getTitle());
        EditorForm editor = new EditorForm(map);
        mapTab.setContent(editor.createContent());
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

    private void deleteMap(MindMap map, TableView<MindMap> table) {
        try {
            apiClient.deleteMap(map.getId());
            loadMaps(table);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Не вдалося видалити: " + e.getMessage()).show();
        }
    }
}
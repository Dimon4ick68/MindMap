package com.mindapp.client.ui;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;
import com.mindapp.client.models.Node;
import com.mindapp.client.patterns.*;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.File;
import java.util.Optional;

public class EditorForm {
    private final MindMap map;
    private final ApiClient apiClient = new ApiClient();
    
    private Canvas canvas;
    private GraphicsContext gc;
    
    // Патерни
    private ThemeFactory currentThemeFactory = new LightThemeFactory();
    private NodeRenderer nodeRenderer = currentThemeFactory.createNodeRenderer();
    private LineStrategy lineStrategy = currentThemeFactory.createLineStrategy();

    // Стан редактора
    private Node selectedNode = null;
    private double dragOffsetX, dragOffsetY;
    
    // Фікс для меню: зберігаємо активне меню, щоб закрити попереднє
    private ContextMenu currentContextMenu;

    public EditorForm(MindMap map) {
        this.map = map;
        if (map.getRootNode() == null) {
             map.setRootNode(new Node("Центральна ідея", 600, 400));
        }
    }

    public BorderPane createContent() {
        BorderPane root = new BorderPane();

        // --- Панель інструментів ---
        TextField titleField = new TextField(map.getTitle());
        Button btnSave = new Button("💾 Зберегти");
        btnSave.setOnAction(e -> {
            map.setTitle(titleField.getText());
            saveMap();
        });

        Button btnAddChild = new Button("➕ Вузол");
        btnAddChild.setOnAction(e -> addChildNode());
        
        Button btnTheme = new Button("🌗 Тема");
        btnTheme.setOnAction(e -> toggleTheme());

        ToolBar toolbar = new ToolBar(
            new Label("Назва:"), titleField, btnSave, 
            new Separator(), 
            btnAddChild, btnTheme
        );

        // --- Полотно ---
        canvas = new Canvas(2000, 2000);
        gc = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseClicked(this::onMouseClicked);

        ScrollPane scrollPane = new ScrollPane(canvas);
        root.setTop(toolbar);
        root.setCenter(scrollPane);

        draw();
        return root;
    }

    // --- Логіка малювання ---

    private void draw() {
        // 1. Очищення фону
        gc.setFill(currentThemeFactory.getBackgroundColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 2. Малювання дерева
        if (map.getRootNode() != null) {
            drawTreeRecursive(map.getRootNode());
        }
        
        // 3. Рамка виділення (Виправлена: з відступом)
        if (selectedNode != null) {
            double padding = 4;
            double w = getActualWidth(selectedNode);
            double h = getActualHeight(selectedNode);
            
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            // Малюємо рамку трохи ширше за сам вузол
            gc.strokeRect(selectedNode.getX() - padding, selectedNode.getY() - padding, w + padding*2, h + padding*2);
        }
    }

    private void drawTreeRecursive(Node current) {
        // Спочатку малюємо лінії (Strategy)
        for (Node child : current.getChildren()) {
            lineStrategy.drawLine(gc, current, child, nodeRenderer);
            drawTreeRecursive(child);
        }

        // Якщо це "Область" (група), малюємо пунктирну рамку ПІД вузлом
        if ("AREA".equals(current.getCategory())) {
            drawAreaBorder(current);
        }

        // Малюємо сам вузол (Bridge)
        nodeRenderer.render(gc, current);
        
        // Малюємо вкладення зверху
        drawAttachment(current);
        
        // Якщо це "Важливо" - додаємо позначку (зірочку або колір)
        if ("IMPORTANT".equals(current.getCategory())) {
            drawImportantMark(current);
        }
    }

    // Малювання пунктирної області
    private void drawAreaBorder(Node node) {
        double w = getActualWidth(node);
        double h = getActualHeight(node);
        double padding = 15; // Область трохи більша за вузол

        gc.save();
        gc.setStroke(Color.GRAY);
        gc.setLineDashes(10); // Пунктир
        gc.setLineWidth(2);
        // Малюємо великий прямокутник навколо вузла
        gc.strokeRect(node.getX() - padding, node.getY() - padding, w + padding*2, h + padding*2);
        
        // Підпис області
        gc.setFill(Color.GRAY);
        gc.fillText("Група: " + node.getText(), node.getX() - padding, node.getY() - padding - 5);
        gc.restore();
    }
    
    private void drawImportantMark(Node node) {
        gc.setFill(Color.RED);
        gc.fillOval(node.getX() - 5, node.getY() - 5, 10, 10); // Червона крапка зліва зверху
    }

    private void drawAttachment(Node node) {
        if (node.getAttachmentPath() == null || "NONE".equals(node.getAttachmentType())) return;

        double w = getActualWidth(node);
        double h = getActualHeight(node);
        
        // Малюємо мініатюру всередині вузла (зміщення залежить від розміру рендерера)
        double imgX = node.getX() + 10;
        double imgY = node.getY() + 35; 
        double imgW = w - 20;
        double imgH = h - 45;

        if ("IMAGE".equals(node.getAttachmentType())) {
            try {
                Image img = new Image(node.getAttachmentPath(), imgW, imgH, true, true);
                gc.drawImage(img, imgX, imgY);
            } catch (Exception e) { /* ignore */ }
        } else if ("VIDEO".equals(node.getAttachmentType())) {
            gc.setFill(Color.BLACK);
            gc.fillRect(imgX, imgY, imgW, imgH);
            gc.setFill(Color.WHITE);
            gc.fillText("▶ VIDEO", imgX + 20, imgY + 30);
        } else if ("FILE".equals(node.getAttachmentType())) {
             gc.setFill(Color.LIGHTGRAY);
             gc.fillRect(imgX, imgY, imgW, imgH);
             gc.setFill(Color.BLACK);
             gc.fillText("📄 FILE", imgX + 10, imgY + 30);
        }
    }

    // --- Дії користувача ---

    // 1. Меню (Фікс бага з множинним відкриттям)
    private void showContextMenu(double screenX, double screenY) {
        // Закриваємо попереднє меню, якщо є
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }

        ContextMenu menu = new ContextMenu();
        currentContextMenu = menu; // Запам'ятовуємо поточне

        MenuItem itemEdit = new MenuItem("✏️ Змінити текст");
        itemEdit.setOnAction(e -> editNodeText());

        MenuItem itemAddChild = new MenuItem("➕ Додати під-вузол");
        itemAddChild.setOnAction(e -> addChildNode());
        
        MenuItem itemDelete = new MenuItem("❌ Видалити вузол");
        itemDelete.setOnAction(e -> deleteSelectedNode());
        
        // Перемикачі
        MenuItem itemArea = new MenuItem(
            "AREA".equals(selectedNode.getCategory()) ? "Скасувати область" : "🔲 Зробити областю"
        );
        itemArea.setOnAction(e -> toggleCategory("AREA"));

        MenuItem itemImportant = new MenuItem(
            "IMPORTANT".equals(selectedNode.getCategory()) ? "Зняти важливість" : "❗ Позначити важливим"
        );
        itemImportant.setOnAction(e -> toggleCategory("IMPORTANT"));

        // Вкладення
        Menu menuAttach = new Menu("📎 Вкладення");
        MenuItem itemImg = new MenuItem("🖼️ Фото");
        itemImg.setOnAction(e -> attachFile("IMAGE"));
        
        MenuItem itemVid = new MenuItem("🎥 Відео");
        itemVid.setOnAction(e -> attachFile("VIDEO"));
        
        MenuItem itemFile = new MenuItem("📄 Файл");
        itemFile.setOnAction(e -> attachFile("FILE"));
        
        MenuItem itemClear = new MenuItem("🗑️ Прибрати вкладення");
        itemClear.setOnAction(e -> clearAttachment());
        
        menuAttach.getItems().addAll(itemImg, itemVid, itemFile, new SeparatorMenuItem(), itemClear);

        menu.getItems().addAll(itemEdit, itemAddChild, new SeparatorMenuItem(), itemImportant, itemArea, menuAttach, new SeparatorMenuItem(), itemDelete);
        menu.show(canvas, screenX, screenY);
    }

    // 2. Фільтрація файлів
    private void attachFile(String type) {
        if (selectedNode == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть файл");

        // Додаємо фільтри залежно від типу
        if ("IMAGE".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        } else if ("VIDEO".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Відео", "*.mp4", "*.avi", "*.mkv"));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Всі файли", "*.*"));
        }

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            selectedNode.setAttachmentType(type);
            selectedNode.setAttachmentPath(file.toURI().toString());
            draw();
        }
    }

    // 3. Перегляд (Preview)
    private void showPreview() {
        if (selectedNode == null || selectedNode.getAttachmentPath() == null) return;

        String type = selectedNode.getAttachmentType();
        String path = selectedNode.getAttachmentPath();

        if ("IMAGE".equals(type)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Перегляд зображення");
            alert.setHeaderText(selectedNode.getText());
            
            ImageView imageView = new ImageView(new Image(path));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(500); // Обмеження висоти
            
            alert.getDialogPane().setContent(new VBox(imageView));
            alert.showAndWait();
        
        } else if ("VIDEO".equals(type)) {
            // Відкриваємо відео у новому вікні
            Stage videoStage = new Stage();
            videoStage.setTitle("Відеоплеєр: " + selectedNode.getText());

            Media media = new Media(path);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            
            mediaView.setFitWidth(800);
            mediaView.setPreserveRatio(true);

            StackPane root = new StackPane(mediaView);
            videoStage.setScene(new Scene(root, 800, 600));
            videoStage.show();
            
            mediaPlayer.play();
            videoStage.setOnCloseRequest(e -> mediaPlayer.stop());
        
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Інформація про файл");
            alert.setHeaderText("Прикріплений файл");
            alert.setContentText("Шлях: " + path);
            alert.showAndWait();
        }
    }

    // --- Допоміжні логічні методи ---

    private void toggleCategory(String category) {
        if (selectedNode == null) return;
        
        if (category.equals(selectedNode.getCategory())) {
            selectedNode.setCategory("NORMAL"); // Якщо вже є, знімаємо
        } else {
            selectedNode.setCategory(category); // Встановлюємо
        }
        draw();
    }

    private void addChildNode() {
        if (selectedNode != null) {
            Node child = new Node("Нова ідея", selectedNode.getX() + 50, selectedNode.getY() + 50);
            selectedNode.getChildren().add(child);
            draw();
        }
    }

    private void editNodeText() {
        if (selectedNode == null) return;
        TextInputDialog dialog = new TextInputDialog(selectedNode.getText());
        dialog.setTitle("Редагування");
        dialog.setHeaderText("Змінити текст:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            selectedNode.setText(text);
            draw();
        });
    }
    
    private void deleteSelectedNode() {
        if (selectedNode == null) return;
        if (selectedNode == map.getRootNode()) return;
        
        Node parent = findParent(map.getRootNode(), selectedNode);
        if (parent != null) {
            parent.getChildren().remove(selectedNode);
            selectedNode = null;
            draw();
        }
    }

    private void clearAttachment() {
        if (selectedNode != null) {
            selectedNode.setAttachmentType("NONE");
            selectedNode.setAttachmentPath(null);
            draw();
        }
    }

    private void toggleTheme() {
        if (currentThemeFactory instanceof LightThemeFactory) currentThemeFactory = new DarkThemeFactory();
        else currentThemeFactory = new LightThemeFactory();
        nodeRenderer = currentThemeFactory.createNodeRenderer();
        lineStrategy = currentThemeFactory.createLineStrategy();
        draw();
    }

    private void onMousePressed(MouseEvent e) {
        // Закриваємо контекстне меню, якщо клікнули деінде
        if (currentContextMenu != null) {
            currentContextMenu.hide();
            currentContextMenu = null;
        }

        Node clickedNode = findNodeAt(map.getRootNode(), e.getX(), e.getY());
        selectedNode = clickedNode;
        
        if (e.getButton() == MouseButton.SECONDARY && selectedNode != null) {
            showContextMenu(e.getScreenX(), e.getScreenY());
        } else if (selectedNode != null) {
            dragOffsetX = e.getX() - selectedNode.getX();
            dragOffsetY = e.getY() - selectedNode.getY();
        }
        draw();
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && selectedNode != null) {
            if (!"NONE".equals(selectedNode.getAttachmentType())) {
                showPreview(); // Відео або фото
            } else {
                editNodeText(); // Текст
            }
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (selectedNode != null) {
            selectedNode.setX(e.getX() - dragOffsetX);
            selectedNode.setY(e.getY() - dragOffsetY);
            draw();
        }
    }
    
    private void onMouseReleased(MouseEvent e) {}

    private Node findNodeAt(Node current, double x, double y) {
        double w = getActualWidth(current);
        double h = getActualHeight(current);
        if (x >= current.getX() && x <= current.getX() + w &&
            y >= current.getY() && y <= current.getY() + h) return current;
        for (Node child : current.getChildren()) {
            Node found = findNodeAt(child, x, y);
            if (found != null) return found;
        }
        return null;
    }

    private Node findParent(Node current, Node target) {
        for (Node child : current.getChildren()) {
            if (child == target) return current;
            Node found = findParent(child, target);
            if (found != null) return found;
        }
        return null;
    }

    private double getActualWidth(Node node) {
        // Область ширша за звичайний вузол
        if ("AREA".equals(node.getCategory())) return 250;
        if (!"NONE".equals(node.getAttachmentType())) return 120;
        return nodeRenderer.getWidth(node);
    }

    private double getActualHeight(Node node) {
        if ("AREA".equals(node.getCategory())) return 200;
        if (!"NONE".equals(node.getAttachmentType())) return 120;
        return nodeRenderer.getHeight(node);
    }

    private void saveMap() {
        try {
            apiClient.saveMap(map);
            new Alert(Alert.AlertType.INFORMATION, "Збережено!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage()).show();
        }
    }
}
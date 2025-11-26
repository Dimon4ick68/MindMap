package com.mindapp.client.ui;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;
import com.mindapp.client.models.Node;
import com.mindapp.client.patterns.DarkThemeFactory;
import com.mindapp.client.patterns.LightThemeFactory;
import com.mindapp.client.patterns.LineStrategy;
import com.mindapp.client.patterns.NodeRenderer;
import com.mindapp.client.patterns.ThemeFactory;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EditorForm {
    private final MindMap map;
    private final ApiClient apiClient = new ApiClient();
    
    private Canvas canvas;
    private GraphicsContext gc;
    
    // --- ПАТЕРНИ ---
    // Abstract Factory: Фабрика тем
    private ThemeFactory currentThemeFactory = new LightThemeFactory();
    // Bridge: Рендеринг вузлів
    private NodeRenderer nodeRenderer = currentThemeFactory.createNodeRenderer();
    // Strategy: Малювання ліній
    private LineStrategy lineStrategy = currentThemeFactory.createLineStrategy();

    // Стан редактора
    private Node selectedNode = null;
    private double dragOffsetX, dragOffsetY;
    
    // Зберігаємо активне меню, щоб закривати попереднє (фікс бага)
    private ContextMenu currentContextMenu;

    public EditorForm(MindMap map) {
        this.map = map;
        // Створюємо корінь, якщо мапа пуста
        if (map.getRootNode() == null) {
             map.setRootNode(new Node("Центральна ідея", 600, 400));
        }
    }

    public BorderPane createContent() {
        BorderPane root = new BorderPane();

        // --- 1. ПАНЕЛЬ ІНСТРУМЕНТІВ ---
        TextField titleField = new TextField(map.getTitle());
        Button btnSave = new Button("💾 Зберегти");
        btnSave.setOnAction(e -> {
            map.setTitle(titleField.getText());
            saveMap();
        });
        
        // Кнопки швидкого доступу
        Button btnAddChild = new Button("➕ Вузол");
        btnAddChild.setOnAction(e -> addChildNode());

        Button btnAddImg = new Button("🖼️ Фото");
        btnAddImg.setOnAction(e -> attachFile("IMAGE"));
        
        Button btnAddVid = new Button("🎥 Відео");
        btnAddVid.setOnAction(e -> attachFile("VIDEO"));
        
        Button btnUrgent = new Button("❗ Важливо");
        btnUrgent.setOnAction(e -> toggleCategory("IMPORTANT"));
        
        Button btnArea = new Button("🔲 Область");
        btnArea.setOnAction(e -> toggleCategory("AREA"));

        Button btnExport = new Button("📷 Експорт");
        btnExport.setOnAction(e -> exportMap());

        Button btnTheme = new Button("🌗 Тема");
        btnTheme.setOnAction(e -> toggleTheme());

        ToolBar toolbar = new ToolBar(
            new Label("Назва:"), titleField, btnSave, 
            new Separator(), 
            btnAddChild, btnAddImg, btnAddVid, btnUrgent, btnArea,
            new Separator(),
            btnExport,
            btnTheme
        );

        // --- 2. ПОЛОТНО (CANVAS) ---
        canvas = new Canvas(3000, 2000); // Велике полотно
        gc = canvas.getGraphicsContext2D();

        // Події миші
        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseClicked(this::onMouseClicked); // Для подвійного кліку

        ScrollPane scrollPane = new ScrollPane(canvas);
        root.setTop(toolbar);
        root.setCenter(scrollPane);

        draw(); // Перше малювання
        return root;
    }

    // --- ЛОГІКА МАЛЮВАННЯ ---

    private void draw() {
        // Фон з фабрики
        gc.setFill(currentThemeFactory.getBackgroundColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Малюємо дерево (Composite)
        if (map.getRootNode() != null) {
            drawTreeRecursive(map.getRootNode());
        }
        
        // Рамка виділення
        if (selectedNode != null) {
            double padding = 4;
            double w = getActualWidth(selectedNode);
            double h = getActualHeight(selectedNode);
            
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(selectedNode.getX() - padding, selectedNode.getY() - padding, w + padding*2, h + padding*2);
        }
    }

    private void drawTreeRecursive(Node current) {
        // 1. Лінії до дітей (Strategy)
        for (Node child : current.getChildren()) {
            lineStrategy.drawLine(gc, current, child, nodeRenderer);
            drawTreeRecursive(child); // Рекурсія
        }

        // 2. Якщо це Область -> малюємо пунктир
        if ("AREA".equals(current.getCategory())) {
            drawAreaBorder(current);
        }

        // 3. Малюємо вузол (Bridge)
        nodeRenderer.render(gc, current);
        
        // 5. Позначка "Важливо"
        if ("IMPORTANT".equals(current.getCategory())) {
            drawImportantMark(current);
        }
    }
    // --- ЕКСПОРТ У ЗОБРАЖЕННЯ ---
    private void exportMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти карту як зображення");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Зображення", "*.png"));
        
        // Пропонуємо ім'я файлу
        fileChooser.setInitialFileName(map.getTitle() + ".png");

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            try {
                // 1. Робимо "знімок" (snapshot) канвасу
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                
                // 2. Конвертуємо JavaFX Image у буферизоване зображення для запису
                // (тут і потрібен javafx-swing)
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                
                new Alert(Alert.AlertType.INFORMATION, "Карту успішно експортовано!").show();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Помилка експорту: " + e.getMessage()).show();
            }
        }
    }

    private void drawAttachment(Node node) {
        if (node.getAttachmentPath() == null || "NONE".equals(node.getAttachmentType())) return;

        double w = getActualWidth(node);
        double h = getActualHeight(node);
        
        // Координати мініатюри всередині вузла
        double imgX = node.getX() + 10;
        double imgY = node.getY() + 35; 
        double imgW = w - 20;
        double imgH = h - 45;

        if ("IMAGE".equals(node.getAttachmentType())) {
            try {
                Image img = new Image(node.getAttachmentPath(), imgW, imgH, true, true);
                gc.drawImage(img, imgX, imgY);
            } catch (Exception e) { /* ігноруємо помилки */ }
        } else if ("VIDEO".equals(node.getAttachmentType())) {
            gc.setFill(Color.BLACK);
            gc.fillRect(imgX, imgY, imgW, imgH);
            gc.setFill(Color.WHITE);
            gc.fillText("▶ VIDEO", imgX + 20, imgY + 40);
        } else if ("FILE".equals(node.getAttachmentType())) {
             gc.setFill(Color.LIGHTGRAY);
             gc.fillRect(imgX, imgY, imgW, imgH);
             gc.setFill(Color.BLACK);
             gc.fillText("📄 FILE", imgX + 10, imgY + 30);
        }
    }
    
    private void drawAreaBorder(Node node) {
        // 1. Знаходимо межі (minX, minY, maxX, maxY) для вузла та всіх його дітей
        Bounds bounds = calculateBounds(node);
        
        double padding = 20; // Відступ
        double x = bounds.minX - padding;
        double y = bounds.minY - padding;
        double w = (bounds.maxX - bounds.minX) + padding * 2;
        double h = (bounds.maxY - bounds.minY) + padding * 2;

        gc.save();
        // Стиль області
        gc.setStroke(Color.GRAY);
        gc.setLineDashes(10); // Пунктир
        gc.setLineWidth(2);
        // Напівпрозора заливка, щоб виділити групу
        gc.setFill(Color.rgb(200, 200, 200, 0.2)); 
        
        gc.fillRect(x, y, w, h);
        gc.strokeRect(x, y, w, h);
        
        // Підпис області (зверху зліва)
        gc.setFill(Color.GRAY);
        gc.setFont(new Font("Arial", 12));
        gc.fillText("📂 Група: " + node.getText(), x, y - 5);
        gc.restore();
    }

    // Допоміжний клас для меж
    private static class Bounds {
        double minX, minY, maxX, maxY;
        public Bounds(double x, double y, double w, double h) {
            this.minX = x; this.minY = y; this.maxX = x + w; this.maxY = y + h;
        }
    }

    // Рекурсивний підрахунок розміру групи
    private Bounds calculateBounds(Node node) {
        double w = getActualWidth(node);
        double h = getActualHeight(node);
        
        Bounds currentBounds = new Bounds(node.getX(), node.getY(), w, h);

        for (Node child : node.getChildren()) {
            Bounds childBounds = calculateBounds(child);
            // Розширюємо межі, щоб вмістити дітей
            currentBounds.minX = Math.min(currentBounds.minX, childBounds.minX);
            currentBounds.minY = Math.min(currentBounds.minY, childBounds.minY);
            currentBounds.maxX = Math.max(currentBounds.maxX, childBounds.maxX);
            currentBounds.maxY = Math.max(currentBounds.maxY, childBounds.maxY);
        }
        return currentBounds;
    }
    
    private void drawImportantMark(Node node) {
        gc.setFill(Color.RED);
        gc.fillOval(node.getX() - 5, node.getY() - 5, 12, 12);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(node.getX() - 5, node.getY() - 5, 12, 12);
    }

    // --- ДІЇ КОРИСТУВАЧА ---

    // Відкриття контекстного меню
    private void showContextMenu(double screenX, double screenY) {
        if (currentContextMenu != null) {
            currentContextMenu.hide();
        }

        ContextMenu menu = new ContextMenu();
        currentContextMenu = menu;

        MenuItem itemEdit = new MenuItem("✏️ Змінити текст");
        itemEdit.setOnAction(e -> editNodeText());

        MenuItem itemAddChild = new MenuItem("➕ Додати під-вузол");
        itemAddChild.setOnAction(e -> addChildNode());
        
        // Кнопка видалення
        MenuItem itemDelete = new MenuItem("❌ Видалити вузол");
        itemDelete.setOnAction(e -> deleteSelectedNode());
        
        // Перемикач "Важливо" (Тоггл)
        MenuItem itemImportant = new MenuItem(
            "IMPORTANT".equals(selectedNode.getCategory()) ? "⚪ Зняти важливість" : "❗ Позначити важливим"
        );
        itemImportant.setOnAction(e -> toggleCategory("IMPORTANT"));

        // Перемикач "Область"
        MenuItem itemArea = new MenuItem(
             "AREA".equals(selectedNode.getCategory()) ? "Зробити звичайним" : "🔲 Зробити областю"
        );
        itemArea.setOnAction(e -> toggleCategory("AREA"));

        // Меню вкладень
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

    // Додавання файлу з фільтром
    private void attachFile(String type) {
        if (selectedNode == null) {
            showAlert("Спочатку виберіть вузол!");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть файл");

        if ("IMAGE".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        } else if ("VIDEO".equals(type)) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Відео", "*.mp4", "*.avi", "*.mkv"));
        }

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            selectedNode.setAttachmentType(type);
            selectedNode.setAttachmentPath(file.toURI().toString());
            draw();
        }
    }

    // Попередній перегляд (Double Click)
    private void showPreview() {
        if (selectedNode == null || selectedNode.getAttachmentPath() == null) return;

        String type = selectedNode.getAttachmentType();
        String path = selectedNode.getAttachmentPath();

        if ("IMAGE".equals(type)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Перегляд");
            alert.setHeaderText(selectedNode.getText());
            
            ImageView imageView = new ImageView(new Image(path));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(500);
            
            alert.getDialogPane().setContent(new VBox(imageView));
            alert.showAndWait();
        
        } else if ("VIDEO".equals(type)) {
            // ВІДЕО ПЛЕЄР
            Stage videoStage = new Stage();
            videoStage.setTitle("Відео: " + selectedNode.getText());

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
        }
    }

    // Логіка перемикання категорії (Toggle)
    private void toggleCategory(String category) {
        if (selectedNode == null) return;
        
        if (category.equals(selectedNode.getCategory())) {
            selectedNode.setCategory("NORMAL"); // Вимикаємо
        } else {
            selectedNode.setCategory(category); // Вмикаємо
        }
        draw();
    }

    private void addChildNode() {
        if (selectedNode != null) {
            Node child = new Node("Нова ідея", selectedNode.getX() + 60, selectedNode.getY() + 60);
            selectedNode.getChildren().add(child);
            draw();
        }
    }

    private void editNodeText() {
        if (selectedNode == null) return;
        TextInputDialog dialog = new TextInputDialog(selectedNode.getText());
        dialog.setTitle("Редагування");
        dialog.setHeaderText("Введіть новий текст:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            selectedNode.setText(text);
            draw();
        });
    }
    
    private void deleteSelectedNode() {
        if (selectedNode == null) return;
        if (selectedNode == map.getRootNode()) {
            showAlert("Не можна видалити кореневий вузол!");
            return;
        }
        
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

    // --- ОБРОБКА МИШІ ---

    private void onMousePressed(MouseEvent e) {
        // Ховаємо меню при кліку
        if (currentContextMenu != null) {
            currentContextMenu.hide();
            currentContextMenu = null;
        }

        Node clickedNode = findNodeAt(map.getRootNode(), e.getX(), e.getY());
        selectedNode = clickedNode;
        
        // Правий клік -> Меню
        if (e.getButton() == MouseButton.SECONDARY && selectedNode != null) {
            showContextMenu(e.getScreenX(), e.getScreenY());
        } 
        // Лівий клік -> Початок перетягування
        else if (selectedNode != null) {
            dragOffsetX = e.getX() - selectedNode.getX();
            dragOffsetY = e.getY() - selectedNode.getY();
        }
        draw();
    }

    private void onMouseClicked(MouseEvent e) {
        // Подвійний клік
        if (e.getClickCount() == 2 && selectedNode != null) {
            if (!"NONE".equals(selectedNode.getAttachmentType())) {
                showPreview(); // Якщо є файл - відкриваємо
            } else {
                editNodeText(); // Якщо немає - редагуємо текст
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

    // --- ДОПОМІЖНІ ---

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
            new Alert(Alert.AlertType.INFORMATION, "Успішно збережено!").show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage()).show();
        }
    }
    
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).show();
    }
}
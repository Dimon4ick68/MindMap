package com.mindapp.client.ui;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import com.mindapp.client.api.ApiClient;
import com.mindapp.client.models.MindMap;
import com.mindapp.client.models.Node;
import com.mindapp.client.patterns.DarkThemeFactory;
import com.mindapp.client.patterns.LightThemeFactory;
import com.mindapp.client.patterns.LineStrategy;
import com.mindapp.client.patterns.NodeRenderer;
import com.mindapp.client.patterns.ThemeFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditorForm {
    private final MindMap map;
    private final ApiClient apiClient = new ApiClient();
    
    private Canvas canvas;
    private GraphicsContext gc;
    private ScrollPane scrollPane; // Посилання на скрол-панель для панорамування
    
    // Патерни
    private ThemeFactory currentThemeFactory = new LightThemeFactory();
    private NodeRenderer nodeRenderer = currentThemeFactory.createNodeRenderer();
    private LineStrategy lineStrategy = currentThemeFactory.createLineStrategy();

    // Стан редактора
    private Set<Node> selectedNodes = new HashSet<>();
    
    // Координати миші
    private double lastMouseX, lastMouseY; 
    private double sceneMouseX, sceneMouseY; // Для панорамування
    
    // Ласо
    private boolean isSelecting = false;
    private double selectionStartX, selectionStartY;
    private double selectionEndX, selectionEndY;

    private ContextMenu currentContextMenu;
    private Timeline autoSaveTimer;
    private Label statusLabel = new Label("Готово");
    
    // Зум
    private double currentZoom = 1.0;
    private Label lblZoom = new Label("100%");

    public EditorForm(MindMap map) {
        this.map = map;
        if (map.getRootNode() == null) {
             map.setRootNode(new Node("Центральна ідея", 1500, 1000));
        }
    }

    public BorderPane createContent() {
        BorderPane root = new BorderPane();

        // --- ПАНЕЛЬ ІНСТРУМЕНТІВ ---
        TextField titleField = new TextField(map.getTitle());
        titleField.setPromptText("Назва мапи");
        
        Button btnSave = new Button("💾");
        btnSave.setTooltip(new Tooltip("Зберегти"));
        btnSave.setOnAction(e -> {
            map.setTitle(titleField.getText());
            saveMap(false);
        });
        
        Button btnAddChild = new Button("➕ Вузол");
        btnAddChild.setOnAction(e -> addChildNode());

        MenuButton btnAttach = new MenuButton("📎");
        MenuItem miPhoto = new MenuItem("🖼️ Фото"); miPhoto.setOnAction(e -> attachFile("IMAGE"));
        MenuItem miVideo = new MenuItem("🎥 Відео"); miVideo.setOnAction(e -> attachFile("VIDEO"));
        MenuItem miFile = new MenuItem("📄 Файл"); miFile.setOnAction(e -> attachFile("FILE"));
        btnAttach.getItems().addAll(miPhoto, miVideo, miFile);
        
        Button btnUrgent = new Button("❗");
        btnUrgent.setTooltip(new Tooltip("Важливо/Звичайно"));
        btnUrgent.setOnAction(e -> toggleCategory("IMPORTANT"));
        
        Button btnArea = new Button("🔲");
        btnArea.setTooltip(new Tooltip("Створити/Прибрати групу"));
        btnArea.setOnAction(e -> toggleCategory("AREA"));

        Button btnExport = new Button("📷");
        btnExport.setTooltip(new Tooltip("Експорт"));
        btnExport.setOnAction(e -> exportMap());

        Button btnTheme = new Button("🌗");
        btnTheme.setTooltip(new Tooltip("Тема"));
        btnTheme.setOnAction(e -> toggleTheme());
        
        // Кнопки зуму
        Button btnZoomIn = new Button("🔍+");
        btnZoomIn.setOnAction(e -> updateZoom(0.1));
        
        Button btnZoomOut = new Button("🔍-");
        btnZoomOut.setOnAction(e -> updateZoom(-0.1));
        
        statusLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
        statusLabel.setPadding(new Insets(4, 0, 0, 0));

        ToolBar toolbar = new ToolBar(
            new Label("Назва:"), titleField, btnSave, 
            new Separator(), 
            btnAddChild, btnAttach, btnUrgent, btnArea,
            new Separator(),
            btnZoomOut, lblZoom, btnZoomIn,
            new Separator(),
            btnExport, btnTheme,
            new Separator(),
            statusLabel
        );

        // --- ПОЛОТНО ---
        canvas = new Canvas(4000, 3000); 
        gc = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);
        canvas.setOnMouseClicked(this::onMouseClicked);

        // Обгортаємо в Group для зуму
        Group canvasGroup = new Group(canvas);
        
        scrollPane = new ScrollPane(canvasGroup);
        
        // !!! ВИПРАВЛЕННЯ КОНФЛІКТУ !!!
        scrollPane.setPannable(false); // Вимикаємо стандартне перетягування лівою кнопкою
        
        // Додаємо зум коліщатком
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double delta = event.getDeltaY() > 0 ? 0.1 : -0.1;
                updateZoom(delta);
                event.consume();
            }
        });

        root.setTop(toolbar);
        root.setCenter(scrollPane);

        draw();
        startAutoSave();

        return root;
    }
    
    // --- ЗУМ ---
    private void updateZoom(double delta) {
        currentZoom += delta;
        if (currentZoom < 0.2) currentZoom = 0.2;
        if (currentZoom > 3.0) currentZoom = 3.0;
        
        Scale scale = new Scale(currentZoom, currentZoom, 0, 0);
        canvas.getTransforms().clear();
        canvas.getTransforms().add(scale);
        
        lblZoom.setText(String.format("%.0f%%", currentZoom * 100));
    }

    // --- МАЛЮВАННЯ ---
    private void draw() {
        gc.setFill(currentThemeFactory.getBackgroundColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (map.getRootNode() != null) {
            drawTreeRecursive(map.getRootNode());
        }
        
        for (Node node : selectedNodes) {
            drawSelectionBorder(node);
        }

        if (isSelecting) {
            double x = Math.min(selectionStartX, selectionEndX);
            double y = Math.min(selectionStartY, selectionEndY);
            double w = Math.abs(selectionEndX - selectionStartX);
            double h = Math.abs(selectionEndY - selectionStartY);

            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.setLineDashes(5);
            gc.strokeRect(x, y, w, h);
            gc.setFill(Color.rgb(0, 0, 255, 0.1));
            gc.fillRect(x, y, w, h);
            gc.setLineDashes(0);
        }
    }

    private void drawSelectionBorder(Node node) {
        double padding = 4;
        double w = getActualWidth(node);
        double h = getActualHeight(node);
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeRect(node.getX() - padding, node.getY() - padding, w + padding*2, h + padding*2);
    }

    private void drawTreeRecursive(Node current) {
        for (Node child : current.getChildren()) {
            lineStrategy.drawLine(gc, current, child, nodeRenderer);
            drawTreeRecursive(child);
        }
        if ("AREA".equals(current.getCategory())) {
            drawAreaBorder(current);
        }
        nodeRenderer.render(gc, current);
        
        if ("IMPORTANT".equals(current.getCategory())) {
            drawImportantMark(current);
        }
    }
    
    private void drawAreaBorder(Node node) {
        Bounds bounds = calculateBounds(node);
        double padding = 20; 
        double x = bounds.minX - padding;
        double y = bounds.minY - padding;
        double w = (bounds.maxX - bounds.minX) + padding * 2;
        double h = (bounds.maxY - bounds.minY) + padding * 2;

        gc.save();
        gc.setStroke(Color.GRAY);
        gc.setLineDashes(10);
        gc.setLineWidth(2);
        gc.setFill(Color.rgb(200, 200, 200, 0.2)); 
        gc.fillRect(x, y, w, h);
        gc.strokeRect(x, y, w, h);
        gc.setFill(Color.GRAY);
        gc.setFont(new Font("Arial", 12));
        gc.fillText("📂 Група: " + node.getText(), x, y - 5);
        gc.restore();
    }

    private void drawImportantMark(Node node) {
        gc.setFill(Color.RED);
        gc.fillOval(node.getX() - 5, node.getY() - 5, 12, 12);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(node.getX() - 5, node.getY() - 5, 12, 12);
    }

    // --- MOUSE HANDLING ---

    private void onMousePressed(MouseEvent e) {
        if (currentContextMenu != null) { currentContextMenu.hide(); currentContextMenu = null; }

        // Для панорамування запам'ятовуємо початкову позицію
        if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.MIDDLE) {
            sceneMouseX = e.getSceneX();
            sceneMouseY = e.getSceneY();
        }

        Node clickedNode = findNodeAt(map.getRootNode(), e.getX(), e.getY());
        
        if (clickedNode != null) {
            // ЛОГІКА ДЛЯ ВУЗЛІВ
            if (e.getButton() == MouseButton.SECONDARY) {
                // Якщо правий клік ПО ВУЗЛУ - показуємо меню
                // Спочатку виділяємо його, якщо ще не виділений
                if (!selectedNodes.contains(clickedNode)) {
                    selectedNodes.clear();
                    selectedNodes.add(clickedNode);
                }
                showContextMenu(e.getScreenX(), e.getScreenY());
            } else {
                // Лівий клік - виділення
                if (e.isControlDown()) {
                    if (selectedNodes.contains(clickedNode)) selectedNodes.remove(clickedNode);
                    else selectedNodes.add(clickedNode);
                } else {
                    if (!selectedNodes.contains(clickedNode)) {
                        selectedNodes.clear();
                        selectedNodes.add(clickedNode);
                    }
                }
            }
            isSelecting = false;
        } else {
            // КЛІК ПО ПУСТОМУ МІСЦЮ
            if (e.getButton() == MouseButton.PRIMARY) {
                // Ліва кнопка - Ласо
                if (!e.isControlDown()) selectedNodes.clear();
                isSelecting = true;
                selectionStartX = e.getX();
                selectionStartY = e.getY();
                selectionEndX = e.getX();
                selectionEndY = e.getY();
            }
            // Права кнопка по пустому - це буде панорамування (в onMouseDragged)
        }

        lastMouseX = e.getX();
        lastMouseY = e.getY();
        draw();
    }

    private void onMouseDragged(MouseEvent e) {
        if (isSelecting) {
            // Малюємо ласо
            selectionEndX = e.getX();
            selectionEndY = e.getY();
            draw();
        } else if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.MIDDLE) {
            // --- ПАНОРАМУВАННЯ (Рух екрану) ---
            // Працює, якщо затиснута права або середня кнопка
            double deltaX = e.getSceneX() - sceneMouseX;
            double deltaY = e.getSceneY() - sceneMouseY;
            
            double hValue = scrollPane.getHvalue();
            double vValue = scrollPane.getVvalue();
            
            // Конвертуємо пікселі в відносні координати скролу
            // (Чутливість можна підкрутити)
            scrollPane.setHvalue(hValue - deltaX / (canvas.getWidth() * currentZoom));
            scrollPane.setVvalue(vValue - deltaY / (canvas.getHeight() * currentZoom));
            
            sceneMouseX = e.getSceneX();
            sceneMouseY = e.getSceneY();
            
        } else if (!selectedNodes.isEmpty() && e.getButton() == MouseButton.PRIMARY) {
            // Перетягування вузлів (тільки лівою кнопкою)
            double deltaX = e.getX() - lastMouseX;
            double deltaY = e.getY() - lastMouseY;
            for (Node node : selectedNodes) {
                node.setX(node.getX() + deltaX);
                node.setY(node.getY() + deltaY);
            }
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            draw();
        }
    }
    
    private void onMouseReleased(MouseEvent e) {
        if (isSelecting) {
            selectNodesInRect();
            isSelecting = false;
            draw();
        }
    }

    // ... (решта методів без змін: selectNodesInRect, onMouseClicked, etc.) ...
    
    private void selectNodesInRect() {
        double minX = Math.min(selectionStartX, selectionEndX);
        double minY = Math.min(selectionStartY, selectionEndY);
        double maxX = Math.max(selectionStartX, selectionEndX);
        double maxY = Math.max(selectionStartY, selectionEndY);
        findNodesInRectRecursive(map.getRootNode(), minX, minY, maxX, maxY);
    }

    private void findNodesInRectRecursive(Node current, double rX, double rY, double rW, double rH) {
        double nodeW = getActualWidth(current);
        double nodeH = getActualHeight(current);
        if (current.getX() < rW && current.getX() + nodeW > rX &&
            current.getY() < rH && current.getY() + nodeH > rY) {
            selectedNodes.add(current);
        }
        for (Node child : current.getChildren()) {
            findNodesInRectRecursive(child, rX, rY, rW, rH);
        }
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && selectedNodes.size() == 1) {
            Node node = selectedNodes.iterator().next();
            if (!"NONE".equals(node.getAttachmentType())) showPreview(node);
            else editNodeText(node);
        }
    }

    private void toggleCategory(String category) {
        for (Node node : selectedNodes) {
            if (category.equals(node.getCategory())) node.setCategory("NORMAL");
            else node.setCategory(category);
        }
        draw();
    }

    private void deleteSelectedNode() {
        if (selectedNodes.isEmpty()) return;
        if (selectedNodes.contains(map.getRootNode())) {
            showAlert("Не можна видалити корінь!");
            return;
        }
        Set<Node> toDelete = new HashSet<>(selectedNodes);
        for (Node node : toDelete) {
             Node parent = findParent(map.getRootNode(), node);
             if (parent != null) parent.getChildren().remove(node);
        }
        selectedNodes.clear();
        draw();
    }

    private void clearAttachment() {
        for (Node node : selectedNodes) {
            node.setAttachmentType("NONE");
            node.setAttachmentPath(null);
        }
        draw();
    }
    
    private void addChildNode() {
        if (selectedNodes.size() == 1) {
            Node parent = selectedNodes.iterator().next();
            Node child = new Node("Нова ідея", parent.getX() + 60, parent.getY() + 60);
            parent.getChildren().add(child);
            selectedNodes.clear();
            selectedNodes.add(child);
            draw();
        } else {
            showAlert("Виберіть ОДИН вузол для додавання нащадка!");
        }
    }

    private void editNodeText(Node node) {
        TextInputDialog dialog = new TextInputDialog(node.getText());
        dialog.setTitle("Редагування");
        dialog.setHeaderText("Змінити текст:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            node.setText(text);
            draw();
        });
    }
    
    private void attachFile(String type) {
        if (selectedNodes.isEmpty()) { showAlert("Виберіть вузол!"); return; }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть файл");
        if ("IMAGE".equals(type)) fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg"));
        else if ("VIDEO".equals(type)) fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Відео", "*.mp4"));

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            for (Node node : selectedNodes) {
                node.setAttachmentType(type);
                node.setAttachmentPath(file.toURI().toString());
            }
            draw();
        }
    }

    private void showPreview(Node node) {
        String type = node.getAttachmentType();
        String path = node.getAttachmentPath();
        if ("IMAGE".equals(type)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Перегляд");
            alert.setHeaderText(node.getText());
            ImageView imageView = new ImageView(new Image(path));
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(500);
            alert.getDialogPane().setContent(new VBox(imageView));
            alert.showAndWait();
        } else if ("VIDEO".equals(type)) {
            Stage videoStage = new Stage();
            videoStage.setTitle("Відео: " + node.getText());
            Media media = new Media(path);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(800);
            mediaView.setPreserveRatio(true);
            StackPane root = new StackPane(mediaView);
            videoStage.setScene(new javafx.scene.Scene(root, 800, 600));
            videoStage.show();
            mediaPlayer.play();
            videoStage.setOnCloseRequest(e -> mediaPlayer.stop());
        }
    }
    
    private void editNodeText() {
        if (selectedNodes.size() == 1) editNodeText(selectedNodes.iterator().next());
    }

    private void showContextMenu(double screenX, double screenY) {
        if (currentContextMenu != null) currentContextMenu.hide();
        ContextMenu menu = new ContextMenu();
        currentContextMenu = menu;

        MenuItem itemEdit = new MenuItem("✏️ Змінити текст");
        itemEdit.setOnAction(e -> editNodeText());
        MenuItem itemAddChild = new MenuItem("➕ Додати під-вузол");
        itemAddChild.setOnAction(e -> addChildNode());
        MenuItem itemDelete = new MenuItem("❌ Видалити вибране");
        itemDelete.setOnAction(e -> deleteSelectedNode());
        
        MenuItem itemImportant = new MenuItem("❗ Перемкнути 'Важливо'");
        itemImportant.setOnAction(e -> toggleCategory("IMPORTANT"));

        MenuItem itemArea = new MenuItem("🔲 Перемкнути 'Область'");
        itemArea.setOnAction(e -> toggleCategory("AREA"));

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

    private void exportMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Зберегти карту як зображення");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Зображення", "*.png"));
        fileChooser.setInitialFileName(map.getTitle() + ".png");
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
                canvas.snapshot(null, writableImage);
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                new Alert(Alert.AlertType.INFORMATION, "Карту успішно експортовано!").show();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Помилка експорту: " + e.getMessage()).show();
            }
        }
    }

    private void startAutoSave() {
        autoSaveTimer = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            saveMap(true);
        }));
        autoSaveTimer.setCycleCount(Timeline.INDEFINITE);
        autoSaveTimer.play();
    }

    private void saveMap(boolean silent) {
        try {
            apiClient.saveMap(map);
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            statusLabel.setText("Збережено о " + time);
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 11px;");
            if (!silent) new Alert(Alert.AlertType.INFORMATION, "Успішно збережено!").show();
        } catch (Exception e) {
            statusLabel.setText("Помилка збереження!");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
            if (!silent) new Alert(Alert.AlertType.ERROR, "Помилка: " + e.getMessage()).show();
        }
    }

    private void toggleTheme() {
        if (currentThemeFactory instanceof LightThemeFactory) currentThemeFactory = new DarkThemeFactory();
        else currentThemeFactory = new LightThemeFactory();
        nodeRenderer = currentThemeFactory.createNodeRenderer();
        lineStrategy = currentThemeFactory.createLineStrategy();
        draw();
    }

    private Node findNodeAt(Node current, double x, double y) {
        for (int i = current.getChildren().size() - 1; i >= 0; i--) {
            Node found = findNodeAt(current.getChildren().get(i), x, y);
            if (found != null) return found;
        }
        if ("AREA".equals(current.getCategory())) {
            double w = nodeRenderer.getWidth(current);
            double h = nodeRenderer.getHeight(current);
            if (x >= current.getX() && x <= current.getX() + w && y >= current.getY() && y <= current.getY() + h) return current;
        } else {
            double w = getActualWidth(current);
            double h = getActualHeight(current);
            if (x >= current.getX() && x <= current.getX() + w && y >= current.getY() && y <= current.getY() + h) return current;
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
        if ("AREA".equals(node.getCategory())) return nodeRenderer.getWidth(node); 
        if (!"NONE".equals(node.getAttachmentType())) return 120;
        return nodeRenderer.getWidth(node);
    }

    private double getActualHeight(Node node) {
        if ("AREA".equals(node.getCategory())) return nodeRenderer.getHeight(node);
        if (!"NONE".equals(node.getAttachmentType())) return 120;
        return nodeRenderer.getHeight(node);
    }
    
    
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).show();
    }
    
    private static class Bounds {
        double minX, minY, maxX, maxY;
        public Bounds(double x, double y, double w, double h) {
            this.minX = x; this.minY = y; this.maxX = x + w; this.maxY = y + h;
        }
    }

    private Bounds calculateBounds(Node node) {
        double w = getActualWidth(node);
        double h = getActualHeight(node);
        if (!"AREA".equals(node.getCategory())) {
             if (!"NONE".equals(node.getAttachmentType())) { w = 140; h = 140; }
             else { w = nodeRenderer.getWidth(node); h = nodeRenderer.getHeight(node); }
        }
        Bounds currentBounds = new Bounds(node.getX(), node.getY(), w, h);
        for (Node child : node.getChildren()) {
            Bounds childBounds = calculateBounds(child);
            currentBounds.minX = Math.min(currentBounds.minX, childBounds.minX);
            currentBounds.minY = Math.min(currentBounds.minY, childBounds.minY);
            currentBounds.maxX = Math.max(currentBounds.maxX, childBounds.maxX);
            currentBounds.maxY = Math.max(currentBounds.maxY, childBounds.maxY);
        }
        return currentBounds;
    }
}
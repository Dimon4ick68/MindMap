package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class SimpleRectangleRenderer implements NodeRenderer {
    
    private static final double PADDING = 10;
    private static final double IMG_SIZE = 100; // Макс розмір картинки
    private static final double FONT_SIZE = 14;

    @Override
    public void render(GraphicsContext gc, Node node) {
        double x = node.getX();
        double y = node.getY();
        double w = getWidth(node);
        double h = getHeight(node);
        
        boolean hasMedia = isMedia(node);

        // 1. Фон
        if ("URGENT".equals(node.getCategory())) {
            gc.setFill(Color.rgb(255, 220, 220)); // Ніжно-червоний
        } else if ("IMPORTANT".equals(node.getCategory())) {
            gc.setFill(Color.rgb(255, 245, 200)); // Ніжно-жовтий
        } else {
            gc.setFill(Color.rgb(240, 248, 255)); // AliceBlue
        }
        
        // Малюємо основну плашку
        gc.fillRoundRect(x, y, w, h, 10, 10); // Заокруглені кути
        gc.setStroke(Color.DARKSLATEBLUE);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, w, h, 10, 10);

        // 2. Вміст (Картинка)
        double contentY = y + PADDING;
        
        if (hasMedia && node.getAttachmentPath() != null) {
            // Центруємо картинку по горизонталі
            double imgX = x + (w - IMG_SIZE) / 2;
            
            if ("IMAGE".equals(node.getAttachmentType())) {
                try {
                    // Завантажуємо із збереженням пропорцій
                    Image img = new Image(node.getAttachmentPath(), IMG_SIZE, IMG_SIZE, true, true);
                    // Коригуємо X, якщо картинка вужча за слот
                    double drawX = x + (w - img.getWidth()) / 2;
                    gc.drawImage(img, drawX, contentY);
                } catch (Exception e) {
                    gc.setFill(Color.RED);
                    gc.fillText("?", x + w/2, contentY + 20);
                }
            } else {
                // Заглушка для відео
                gc.setFill(Color.BLACK);
                gc.fillRect(imgX, contentY, IMG_SIZE, IMG_SIZE * 0.6); // 16:9
                gc.setFill(Color.WHITE);
                gc.fillText("▶", imgX + IMG_SIZE/2 - 5, contentY + (IMG_SIZE * 0.6)/2 + 5);
            }
            contentY += (IMG_SIZE * 0.7); // Зсуваємо текст вниз
        }

        // 3. Текст
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        // Текст чітко по центру ширини
        gc.fillText(node.getText(), x + w / 2, y + h - PADDING);
        gc.setTextAlign(TextAlignment.LEFT); // Скидаємо
    }

    @Override
    public double getWidth(Node node) {
        double textWidth = (node.getText() != null ? node.getText().length() * 8 : 20);
        // Ширина = максимум між текстом і картинкою + відступи
        double contentWidth = Math.max(textWidth, isMedia(node) ? IMG_SIZE : 0);
        return contentWidth + PADDING * 3;
    }

    @Override
    public double getHeight(Node node) {
        double h = PADDING * 2 + FONT_SIZE + 5; 
        if (isMedia(node)) {
            h += (IMG_SIZE * 0.7) + 5; // Місце під картинку
        }
        return h;
    }
    
    private boolean isMedia(Node node) {
        return "IMAGE".equals(node.getAttachmentType()) || "VIDEO".equals(node.getAttachmentType());
    }
}
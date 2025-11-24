package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SimpleRectangleRenderer implements NodeRenderer {
    
    private static final double PADDING = 10;
    private static final double TEXT_HEIGHT = 20;

    @Override
    public void render(GraphicsContext gc, Node node) {
        double x = node.getX();
        double y = node.getY();
        double w = getWidth(node);
        double h = getHeight(node);

        // 1. Малюємо фон (з урахуванням категорії)
        if ("URGENT".equals(node.getCategory())) {
            gc.setFill(Color.RED); // Термінові - червоні
        } else if ("IMPORTANT".equals(node.getCategory())) {
            gc.setFill(Color.ORANGE); // Важливі - помаранчеві
        } else {
            gc.setFill(Color.LIGHTBLUE); // Звичайні - сині
        }
        
        gc.fillRect(x, y, w, h);
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(1.5);
        gc.strokeRect(x, y, w, h);

        // 2. Малюємо текст
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 14));
        gc.fillText(node.getText(), x + PADDING, y + 20);

        // 3. Малюємо вкладення (Картинка або Відео-прев'ю)
        if ("IMAGE".equals(node.getAttachmentType()) && node.getAttachmentPath() != null) {
            try {
                // Завантажуємо картинку (спрощено, краще кешувати)
                Image img = new Image(node.getAttachmentPath(), w - 20, h - 40, true, true);
                gc.drawImage(img, x + 10, y + 30);
            } catch (Exception e) {
                gc.fillText("[IMG ERROR]", x + 10, y + 50);
            }
        } else if ("VIDEO".equals(node.getAttachmentType())) {
            // Для відео малюємо заглушку "Play"
            gc.setFill(Color.BLACK);
            gc.fillRect(x + 10, y + 30, w - 20, h - 40);
            gc.setFill(Color.WHITE);
            gc.fillPolygon(new double[]{x+w/2-10, x+w/2-10, x+w/2+15}, 
                           new double[]{y+h/2-10, y+h/2+10, y+h/2}, 3);
            gc.fillText("VIDEO", x + 15, y + h - 5);
        }
    }

    @Override
    public double getWidth(Node node) {
        // Якщо є картинка, вузол ширший
        if ("IMAGE".equals(node.getAttachmentType()) || "VIDEO".equals(node.getAttachmentType())) {
            return 150;
        }
        return (node.getText() != null ? node.getText().length() * 8 : 20) + PADDING * 2;
    }

    @Override
    public double getHeight(Node node) {
        // Якщо є картинка, вузол вищий
        if ("IMAGE".equals(node.getAttachmentType()) || "VIDEO".equals(node.getAttachmentType())) {
            return 130;
        }
        return 30;
    }
}
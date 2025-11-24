package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class OvalRenderer implements NodeRenderer {
    @Override
    public void render(GraphicsContext gc, Node node) {
        double w = getWidth(node);
        double h = getHeight(node);
        
        // Темний стиль
        gc.setFill(Color.DARKSLATEGRAY);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2);
        
        gc.fillOval(node.getX(), node.getY(), w, h);
        gc.strokeOval(node.getX(), node.getY(), w, h);
        
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 14));
        gc.fillText(node.getText(), node.getX() + 15, node.getY() + h / 1.5);
    }

    @Override
    public double getWidth(Node node) {
        return (node.getText().length() * 8) + 30;
    }

    @Override
    public double getHeight(Node node) {
        return 40;
    }
}

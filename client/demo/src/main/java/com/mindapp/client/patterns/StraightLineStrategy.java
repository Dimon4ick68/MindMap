package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Патерн Strategy: Конкретна стратегія (Пряма лінія)
public class StraightLineStrategy implements LineStrategy {
    @Override
    public void drawLine(GraphicsContext gc, Node parent, Node child, NodeRenderer renderer) {
        double startX = parent.getX() + renderer.getWidth(parent) / 2;
        double startY = parent.getY() + renderer.getHeight(parent) / 2;
        
        double endX = child.getX() + renderer.getWidth(child) / 2;
        double endY = child.getY() + renderer.getHeight(child) / 2;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(startX, startY, endX, endY);
    }
}
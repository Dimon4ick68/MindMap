package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;

import javafx.scene.canvas.GraphicsContext;

public interface LineStrategy {
    // Перевірте, чи є цей метод і чи всі аргументи на місці
    void drawLine(GraphicsContext gc, Node parent, Node child, NodeRenderer renderer);
}

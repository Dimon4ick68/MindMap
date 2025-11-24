package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;
import javafx.scene.canvas.GraphicsContext;

// Патерн Bridge: Інтерфейс реалізації (Implementor)
public interface NodeRenderer {
    void render(GraphicsContext gc, Node node);
    double getWidth(Node node);
    double getHeight(Node node);
}
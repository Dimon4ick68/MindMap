package com.mindapp.client.patterns;

import com.mindapp.client.models.Node;

import javafx.scene.canvas.GraphicsContext;

public interface LineStrategy {
    void drawLine(GraphicsContext gc, Node parent, Node child, NodeRenderer renderer);
}

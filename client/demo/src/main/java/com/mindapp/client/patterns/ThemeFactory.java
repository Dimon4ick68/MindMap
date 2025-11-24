package com.mindapp.client.patterns;

import javafx.scene.paint.Color;

public interface ThemeFactory {
    // Створює сімейство об'єктів:
    NodeRenderer createNodeRenderer();
    LineStrategy createLineStrategy();
    Color getBackgroundColor();
}

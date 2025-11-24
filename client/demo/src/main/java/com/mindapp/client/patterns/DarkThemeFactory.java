package com.mindapp.client.patterns;

import javafx.scene.paint.Color;

public class DarkThemeFactory implements ThemeFactory {
    @Override
    public NodeRenderer createNodeRenderer() {
        return new OvalRenderer(); // Інший вигляд вузлів
    }

    @Override
    public LineStrategy createLineStrategy() {
        return new StraightLineStrategy(); // Можна лишити прямі або додати криві
    }

    @Override
    public Color getBackgroundColor() {
        return Color.rgb(30, 30, 30); // Темний фон
    }
}
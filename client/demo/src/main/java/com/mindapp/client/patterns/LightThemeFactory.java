package com.mindapp.client.patterns;

import javafx.scene.paint.Color;

public class LightThemeFactory implements ThemeFactory {
    @Override
    public NodeRenderer createNodeRenderer() {
        return new SimpleRectangleRenderer(); // Вже нами написаний
    }

    @Override
    public LineStrategy createLineStrategy() {
        return new StraightLineStrategy(); // Вже нами написана
    }

    @Override
    public Color getBackgroundColor() {
        return Color.WHITE;
    }
}

package com.mindapp.client.patterns;

import javafx.scene.paint.Color;

public class LightThemeFactory implements ThemeFactory {
    @Override
    public NodeRenderer createNodeRenderer() {
        // Рамка темна, текст чорний, фон світлий (AliceBlue)
        return new SimpleRectangleRenderer(Color.DARKSLATEBLUE, Color.BLACK, Color.rgb(240, 248, 255));
    }

    @Override
    public LineStrategy createLineStrategy() {
        // Лінії чорні
        return new StraightLineStrategy(Color.BLACK);
    }

    @Override
    public Color getBackgroundColor() {
        return Color.WHITE;
    }
}

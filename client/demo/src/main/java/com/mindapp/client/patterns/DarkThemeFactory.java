package com.mindapp.client.patterns;

import javafx.scene.paint.Color;

public class DarkThemeFactory implements ThemeFactory {
    @Override
    public NodeRenderer createNodeRenderer() {
        // Рамка світла, текст білий, фон вузла темно-сірий
        return new SimpleRectangleRenderer(Color.LIGHTGRAY, Color.WHITE, Color.rgb(60, 63, 65));
    }

    @Override
    public LineStrategy createLineStrategy() {
        // Лінії білі (щоб було видно на темному)
        return new StraightLineStrategy(Color.WHITE);
    }

    @Override
    public Color getBackgroundColor() {
        // Фон полотна темний
        return Color.rgb(43, 43, 43);
    }
}
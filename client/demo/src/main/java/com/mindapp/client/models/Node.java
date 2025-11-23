package com.mindapp.client.models;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private Long id;
    private String text;
    private double x;
    private double y;
    private List<Node> children = new ArrayList<>();

    public Node() {}
    public Node(String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    // Гетери та сетери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public List<Node> getChildren() { return children; }
    public void setChildren(List<Node> children) { this.children = children; }
}
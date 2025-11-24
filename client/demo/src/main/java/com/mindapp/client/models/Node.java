package com.mindapp.client.models;

import java.util.ArrayList;
import java.util.List;

public class Node implements Cloneable {
    private Long id;
    private String text;
    private double x;
    private double y;
    
    // Вкладення та категорії
    private String attachmentType = "NONE"; // "IMAGE", "VIDEO", "FILE"
    private String attachmentPath;
    private String category = "NORMAL";
    
    // --- ОСЬ ЦЬОГО НЕ ВИСТАЧАЛО ---
    private String borderColor; 
    // ------------------------------
    
    private List<Node> children = new ArrayList<>();

    public Node() {}

    public Node(String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    @Override
    public Node clone() {
        try {
            Node cloned = (Node) super.clone();
            cloned.setId(null);
            
            // Копіюємо всі поля
            cloned.attachmentType = this.attachmentType;
            cloned.attachmentPath = this.attachmentPath;
            cloned.category = this.category;
            cloned.borderColor = this.borderColor; // Не забуваємо про копіювання бордера
            
            cloned.children = new ArrayList<>();
            for (Node child : this.children) {
                cloned.children.add(child.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // Гетери та Сетери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // --- ГЕТЕРИ ТА СЕТЕРИ ДЛЯ НОВОГО ПОЛЯ ---
    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }
    // ----------------------------------------
    
    public List<Node> getChildren() { return children; }
    public void setChildren(List<Node> children) { this.children = children; }
}
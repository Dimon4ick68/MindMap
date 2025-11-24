package com.mindapp.server.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private double x;
    private double y;

    // --- НОВІ ПОЛЯ (Для ТЗ) ---
    
    // Тип вкладення: "NONE", "IMAGE", "VIDEO", "FILE"
    private String attachmentType = "NONE";
    
    // Шлях до файлу або URL
    private String attachmentPath;
    
    // Категорія/Терміновість: "NORMAL", "URGENT", "IMPORTANT"
    private String category = "NORMAL";
    
    // Колір для обведення (якщо це корінь області)
    private String borderColor; 

    // ---------------------------

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_node_id")
    private List<Node> children = new ArrayList<>();

    public Node() {}

    // Гетери та Сетери для нових полів
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

    public String getBorderColor() { return borderColor; }
    public void setBorderColor(String borderColor) { this.borderColor = borderColor; }

    public List<Node> getChildren() { return children; }
    public void setChildren(List<Node> children) { this.children = children; }
}
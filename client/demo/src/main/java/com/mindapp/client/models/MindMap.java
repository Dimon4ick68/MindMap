package com.mindapp.client.models;

public class MindMap {
    private Long id;
    private String title;
    private String ownerUserId;
    private Node rootNode;

    public MindMap() {}
    public MindMap(String title, String ownerUserId) {
        this.title = title;
        this.ownerUserId = ownerUserId;
        this.rootNode = new Node("Кореневий вузол", 400, 300); // Дефолтний вузол
    }

    // Гетери та сетери
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public Node getRootNode() { return rootNode; }
    public void setRootNode(Node rootNode) { this.rootNode = rootNode; }
}
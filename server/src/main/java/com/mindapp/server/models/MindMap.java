package com.mindapp.server.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity // 
@Table(name = "mind_maps") // 
public class MindMap {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 
    private Long id;

    private String title; // 
    
    private String ownerUserId; //

    
  
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "root_node_id", referencedColumnName = "id")
    private Node rootNode;
    
   
    
    public MindMap() {}

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(String ownerUserId) { this.ownerUserId = ownerUserId; }
    public Node getRootNode() { return rootNode; }
    public void setRootNode(Node rootNode) { this.rootNode = rootNode; }
}
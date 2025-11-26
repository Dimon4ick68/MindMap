package com.mindapp.server.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindapp.server.models.MindMap;
import com.mindapp.server.services.MindMapService;

@RestController 
@RequestMapping("/api/maps") 
public class MindMapController {

    @Autowired 
    private MindMapService mindMapService;

    @GetMapping
    public List<MindMap> getMapsForUser(@RequestParam String userId) {
        return mindMapService.findMapsByUserId(userId);
    }

    @PostMapping
    public MindMap createMap(@RequestBody MindMap map) {
        return mindMapService.saveMap(map);
    }
    
    // --- НОВИЙ МЕТОД ---
    @DeleteMapping("/{id}")
    public void deleteMap(@PathVariable Long id) {
        mindMapService.deleteMap(id);
    }
}
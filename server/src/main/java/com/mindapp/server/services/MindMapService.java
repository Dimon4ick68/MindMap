package com.mindapp.server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mindapp.server.models.MindMap;
import com.mindapp.server.repositories.MindMapRepository;

@Service // 
public class MindMapService {

    @Autowired // 
    private MindMapRepository mindMapRepository;

   
    public List<MindMap> findMapsByUserId(String userId) {
        return mindMapRepository.findByOwnerUserId(userId);
    }
    
    public void deleteMap(Long id) {
        mindMapRepository.deleteById(id);
    }
    
    public MindMap saveMap(MindMap map) {
        return mindMapRepository.save(map);
    }
}

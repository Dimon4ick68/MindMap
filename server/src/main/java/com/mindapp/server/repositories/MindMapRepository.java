package com.mindapp.server.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindapp.server.models.MindMap;



public interface MindMapRepository extends JpaRepository<MindMap, Long> {

    
    List<MindMap> findByOwnerUserId(String userId);
}
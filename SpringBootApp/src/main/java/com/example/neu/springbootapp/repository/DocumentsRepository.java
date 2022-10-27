package com.example.neu.springbootapp.repository;

import com.example.neu.springbootapp.model.Documents;
import com.example.neu.springbootapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.print.Doc;
import java.util.List;
import java.util.UUID;

public interface DocumentsRepository  extends JpaRepository<Documents, Integer> {

    Documents findById(UUID uuid);
    List<Documents> findByUserId(UUID uuid);

}

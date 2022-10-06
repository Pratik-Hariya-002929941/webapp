package com.example.neu.springbootapp.repository;

import com.example.neu.springbootapp.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    Users findById(UUID uuid);

    Users findByUsername(String username);
}

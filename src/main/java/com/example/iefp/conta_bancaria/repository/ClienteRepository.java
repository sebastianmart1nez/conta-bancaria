package com.example.iefp.conta_bancaria.repository;

import com.example.iefp.conta_bancaria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByUsername(String username);
    boolean existsByUsername(String username);
}

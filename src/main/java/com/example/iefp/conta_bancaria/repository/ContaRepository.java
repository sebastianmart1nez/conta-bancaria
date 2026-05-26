package com.example.iefp.conta_bancaria.repository;

import com.example.iefp.conta_bancaria.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {
    List<Conta> findByClienteId(UUID clienteId);
    long countByClienteId(UUID clienteId);
    Optional<Conta> findByIban(String iban);
    boolean existsByIban(String iban);
}

package com.example.iefp.conta_bancaria.repository;

import com.example.iefp.conta_bancaria.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    List<Transacao> findByContaOrigem_IdContaOrContaDestino_IdContaOrderByDataHoraDesc(UUID idOrigem, UUID idDestino);

    List<Transacao> findTop10ByOrderByDataHoraDesc();

    @Query("SELECT t FROM Transacao t WHERE t.contaOrigem.cliente.id = :idCliente OR t.contaDestino.cliente.id = :idCliente ORDER BY t.dataHora DESC")
    List<Transacao> findTop10ByClienteIdOrderByDataHoraDesc(@Param("idCliente") UUID idCliente);

    @Query("SELECT COUNT(t) FROM Transacao t WHERE t.contaOrigem.cliente.id = :idCliente OR t.contaDestino.cliente.id = :idCliente")
    long countByClienteId(@Param("idCliente") UUID idCliente);
}

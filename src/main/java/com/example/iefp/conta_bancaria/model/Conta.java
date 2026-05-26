package com.example.iefp.conta_bancaria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contas")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idConta;

    @Column(nullable = false, unique = true, length = 25)
    private String iban;

    @Column(nullable = false)
    private String titular;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @OneToMany(mappedBy = "contaOrigem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transacao> transacoesOrigem = new ArrayList<>();

    @OneToMany(mappedBy = "contaDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transacao> transacoesDestino = new ArrayList<>();

    public Conta() {}

    public Conta(String iban, String titular, BigDecimal saldo, LocalDateTime dataCriacao, Cliente cliente) {
        this.iban = iban;
        this.titular = titular;
        this.saldo = saldo;
        this.dataCriacao = dataCriacao;
        this.cliente = cliente;
    }

    public UUID getIdConta() { return idConta; }
    public void setIdConta(UUID idConta) { this.idConta = idConta; }
    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<Transacao> getTransacoesOrigem() { return transacoesOrigem; }
    public void setTransacoesOrigem(List<Transacao> transacoesOrigem) { this.transacoesOrigem = transacoesOrigem; }
    public List<Transacao> getTransacoesDestino() { return transacoesDestino; }
    public void setTransacoesDestino(List<Transacao> transacoesDestino) { this.transacoesDestino = transacoesDestino; }
}

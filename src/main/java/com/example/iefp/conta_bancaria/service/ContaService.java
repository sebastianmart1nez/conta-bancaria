package com.example.iefp.conta_bancaria.service;

import com.example.iefp.conta_bancaria.model.Cliente;
import com.example.iefp.conta_bancaria.model.Conta;
import com.example.iefp.conta_bancaria.model.Transacao;
import com.example.iefp.conta_bancaria.repository.ClienteRepository;
import com.example.iefp.conta_bancaria.repository.ContaRepository;
import com.example.iefp.conta_bancaria.repository.TransacaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final ClienteRepository clienteRepository;

    public ContaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository, ClienteRepository clienteRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.clienteRepository = clienteRepository;
    }

    private String gerarIBAN() {
        String iban;
        do {
            String base = UUID.randomUUID().toString().replace("-", "").substring(0, 21).toUpperCase();
            iban = "PT50" + base;
        } while (contaRepository.existsByIban(iban));
        return iban;
    }

    @Transactional
    public Cliente registrarCliente(String username, String password, String nome) {
        if (clienteRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username já existe");
        }
        Cliente cliente = new Cliente(username, password, nome, LocalDateTime.now());
        cliente = clienteRepository.save(cliente);

        Conta conta = new Conta();
        conta.setIban(gerarIBAN());
        conta.setTitular(nome);
        conta.setSaldo(BigDecimal.ZERO);
        conta.setDataCriacao(LocalDateTime.now());
        conta.setCliente(cliente);
        contaRepository.save(conta);

        return cliente;
    }

    public Cliente autenticarCliente(String username, String password) {
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        if (!cliente.getPassword().equals(password)) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        return cliente;
    }

    @Transactional
    public Conta depositar(UUID idConta, UUID idCliente, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser positivo");
        }
        Conta conta = contaRepository.findById(idConta)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
        if (!conta.getCliente().getId().equals(idCliente)) {
            throw new IllegalArgumentException("Conta não pertence ao cliente");
        }
        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setTipo("DEPOSITO");
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaDestino(conta);
        transacao.setDescricao("Depósito de " + valor + " na conta " + conta.getTitular());
        transacaoRepository.save(transacao);

        return conta;
    }

    @Transactional
    public Conta levantar(UUID idConta, UUID idCliente, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do levantamento deve ser positivo");
        }
        Conta conta = contaRepository.findById(idConta)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
        if (!conta.getCliente().getId().equals(idCliente)) {
            throw new IllegalArgumentException("Conta não pertence ao cliente");
        }
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        Transacao transacao = new Transacao();
        transacao.setTipo("LEVANTAMENTO");
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaOrigem(conta);
        transacao.setDescricao("Levantamento de " + valor + " da conta " + conta.getTitular());
        transacaoRepository.save(transacao);

        return conta;
    }

    @Transactional
    public void transferir(UUID idOrigem, String ibanDestino, UUID idCliente, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser positivo");
        }
        Conta origem = contaRepository.findById(idOrigem)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));
        if (!origem.getCliente().getId().equals(idCliente)) {
            throw new IllegalArgumentException("Conta de origem não pertence ao cliente");
        }
        Conta destino = contaRepository.findByIban(ibanDestino)
                .orElseThrow(() -> new IllegalArgumentException("IBAN de destino não encontrado"));
        if (origem.getIdConta().equals(destino.getIdConta())) {
            throw new IllegalArgumentException("Não pode transferir para a própria conta");
        }
        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente na conta de origem");
        }

        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));
        contaRepository.save(origem);
        contaRepository.save(destino);

        Transacao transacao = new Transacao();
        transacao.setTipo("TRANSFERENCIA");
        transacao.setValor(valor);
        transacao.setDataHora(LocalDateTime.now());
        transacao.setContaOrigem(origem);
        transacao.setContaDestino(destino);
        transacao.setDescricao("Transferência de " + valor + " de " + origem.getTitular() + " para " + destino.getTitular() + " (" + ibanDestino + ")");
        transacaoRepository.save(transacao);
    }

    public Conta obterContaDoCliente(UUID idCliente) {
        List<Conta> contas = contaRepository.findByClienteId(idCliente);
        if (contas.isEmpty()) {
            throw new IllegalArgumentException("Cliente não tem conta bancária");
        }
        return contas.get(0);
    }

    public List<Conta> listarContas(UUID idCliente) {
        return contaRepository.findByClienteId(idCliente);
    }

    public Conta obterConta(UUID id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
    }

    public List<Transacao> obterExtrato(UUID idConta) {
        return transacaoRepository
                .findByContaOrigem_IdContaOrContaDestino_IdContaOrderByDataHoraDesc(idConta, idConta);
    }

    public long getTotalContas(UUID idCliente) {
        return contaRepository.countByClienteId(idCliente);
    }

    public BigDecimal getSaldoTotal(UUID idCliente) {
        return contaRepository.findByClienteId(idCliente).stream()
                .map(Conta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getTotalMovimentos(UUID idCliente) {
        return transacaoRepository.countByClienteId(idCliente);
    }

    public List<Transacao> getTransacoesRecentes(UUID idCliente) {
        return transacaoRepository.findTop10ByClienteIdOrderByDataHoraDesc(idCliente);
    }

    public Cliente getCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
    }
}

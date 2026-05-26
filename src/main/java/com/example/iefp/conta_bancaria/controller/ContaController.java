package com.example.iefp.conta_bancaria.controller;

import com.example.iefp.conta_bancaria.service.ContaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    private UUID getClienteId(HttpSession session) {
        return (UUID) session.getAttribute("clienteId");
    }

    private String checkAuth(HttpSession session) {
        if (getClienteId(session) == null) {
            return "redirect:/login";
        }
        return null;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth !=null) return auth;

        UUID idCliente = getClienteId(session);
        model.addAttribute("cliente", contaService.getCliente(idCliente));
        model.addAttribute("lista", contaService.listarContas(idCliente));
        model.addAttribute("totalContas", contaService.getTotalContas(idCliente));
        model.addAttribute("saldoTotal", contaService.getSaldoTotal(idCliente));
        model.addAttribute("totalMovimentos", contaService.getTotalMovimentos(idCliente));
        model.addAttribute("transacoesRecentes", contaService.getTransacoesRecentes(idCliente));
        var contas = contaService.listarContas(idCliente);
        if (!contas.isEmpty()) {
            model.addAttribute("contaId", contas.get(0).getIdConta());
        }
        return "dashboard";
    }

    @GetMapping("/depositar")
    public String depositarForm(HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        UUID idCliente = getClienteId(session);
        model.addAttribute("conta", contaService.obterContaDoCliente(idCliente));
        return "depositar";
    }

    @PostMapping("/depositar")
    public String depositar(@RequestParam UUID idConta, @RequestParam BigDecimal valor, HttpSession session, RedirectAttributes attr) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        try{
            contaService.depositar(idConta, getClienteId(session), valor);
            attr.addFlashAttribute("sucesso", "Depósito realizado com sucesso!");
        } catch (Exception e) {
            attr.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/contas/depositar";
    }

    @GetMapping("/levantar")
    public String levantarForm(HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        UUID idCliente = getClienteId(session);
        model.addAttribute("conta", contaService.obterContaDoCliente(idCliente));
        return "levantar";
    }

    @PostMapping("/levantar")
    public String levantar(@RequestParam UUID idConta, @RequestParam BigDecimal valor, HttpSession session, RedirectAttributes attr) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        try{
            contaService.levantar(idConta, getClienteId(session), valor);
            attr.addFlashAttribute("sucesso", "Levantamento realizado com sucesso!");
        } catch (Exception e){
            attr.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/contas/levantar";
    }

    @GetMapping("/transferir")
    public String transferirForm(HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        UUID idCliente = getClienteId(session);
        model.addAttribute("conta", contaService.obterContaDoCliente(idCliente));
        return "transferir";
    }

    @PostMapping("/transferir")
    public String transferir(@RequestParam UUID idContaOrigem,
                             @RequestParam String ibanDestino,
                             @RequestParam BigDecimal valor,
                             HttpSession session,
                             RedirectAttributes attr) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        try {
            contaService.transferir(idContaOrigem, ibanDestino, getClienteId(session), valor);
            attr.addFlashAttribute("sucesso", "Transferência realizada com sucesso!");
        } catch (Exception e) {
            attr.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/contas/transferir";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable UUID id, HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        try {
            model.addAttribute("conta", contaService.obterConta(id));
            model.addAttribute("extrato", contaService.obterExtrato(id));
            return "conta-detalhe";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "redirect:/contas";
        }
    }

    @GetMapping("/{id}/movimentos")
    public String movimentos(@PathVariable UUID id, HttpSession session, Model model) {
        String auth = checkAuth(session);
        if (auth != null) return auth;

        try {
            model.addAttribute("conta", contaService.obterConta(id));
            model.addAttribute("movimentos", contaService.obterExtrato(id));
            return "movimentos";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "redirect:/contas";
        }
    }
}

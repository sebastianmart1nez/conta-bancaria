package com.example.iefp.conta_bancaria.controller;

import com.example.iefp.conta_bancaria.model.Cliente;
import com.example.iefp.conta_bancaria.service.ContaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final ContaService contaService;

    public AuthController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(){
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, RedirectAttributes attr){
        try {
            Cliente cliente = contaService.autenticarCliente(username, password);
            session.setAttribute("clienteId", cliente.getId());
            session.setAttribute("clienteNome", cliente.getNome());
            return "redirect:/contas";
        } catch (Exception e) {
            attr.addFlashAttribute("erro", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/registro")
    public String registroForm(){
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String username, @RequestParam String password, @RequestParam String nome, HttpSession session, RedirectAttributes attr){
        try {
            Cliente cliente = contaService.registrarCliente(username, password, nome);
            session.setAttribute("clienteId", cliente.getId());
            session.setAttribute("clienteNome", cliente.getNome());
            return "redirect:/contas";
        } catch (Exception e) {
            attr.addFlashAttribute("erro", e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/login";
    }
}

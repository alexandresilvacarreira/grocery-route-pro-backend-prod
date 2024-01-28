package pt.upskill.groceryroutepro.controllers;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.web.bind.annotation.*;

import pt.upskill.groceryroutepro.services.AuthService;

@RestController
@Component
public class AuthController {

    @Autowired
    AuthService authService;

    // O loginForm requer este endpoint para redireccionar o utilizador no caso de pedidos efetuados sem autenticação.
    // Deverá redireccionar para a página de login do frontend
    @GetMapping("/login")
    public void login() {
    }

    @PostMapping("/logout")
    public void logout() {
    }


}

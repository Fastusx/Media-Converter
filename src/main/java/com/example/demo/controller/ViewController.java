package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

    @GetMapping("/")
    public ModelAndView mainPage() {
        return new ModelAndView("converter");
    }

    @GetMapping("/createAccount")
    public ModelAndView showCreateAccountForm() {
        return new ModelAndView("createAccount");
    }

    @GetMapping("/login")
    public ModelAndView showLoginForm() {
        return new ModelAndView("login");
    }

}
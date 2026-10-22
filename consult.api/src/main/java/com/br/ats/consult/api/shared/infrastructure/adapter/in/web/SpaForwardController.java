package com.br.ats.consult.api.shared.infrastructure.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/login", "/home", "/jobs", "/jobs/**", "/candidates", "/clients",
            "/processes", "/processes/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}

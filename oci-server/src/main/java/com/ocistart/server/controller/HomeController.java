package com.ocistart.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController extends BaseController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        return "home";
    }
}

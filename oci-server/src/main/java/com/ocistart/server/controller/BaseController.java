package com.ocistart.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public abstract class BaseController {

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("appName", "OCI Start");
        model.addAttribute("appVersion", "1.0.0");
    }
}

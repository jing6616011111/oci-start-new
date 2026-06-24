package com.ocistart.server.controller;

import com.ocistart.server.service.BootTotalInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

    @Resource
    private BootTotalInstanceService bootTotalInstanceService;

    @GetMapping
    public String dashboard(Model model) {
        Map<String, Long> stats = bootTotalInstanceService.getStatistics();
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
